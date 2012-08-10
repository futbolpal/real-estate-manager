package reman.client.app.finance.accounts;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import reman.client.app.finance.FinanceManager;
import reman.client.app.finance.TransactionType;
import reman.client.app.finance.accounts.io.FileItem;
import reman.client.app.finance.accounts.io.ItemRegisterResult;
import reman.client.app.finance.exceptions.NameAlreadyExistsException;
import reman.client.app.finance.journals.Journal;
import reman.common.database.exceptions.DatabaseException;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * This class is responsible for import/export of the Accounts and associated AcctActionCategory objects.  Generic methods are provided
 * for collection of FileItem objects and specific methods are provided for '.xml' interaction.
 * @author Scott
 *
 */
public class AccountImportExport {

	private Hashtable<String, Account> root_accts_;

	/**
	 * 
	 * @param root_accounts From the account manager
	 */
	public AccountImportExport(Hashtable<String, Account> root_accounts) {
		this.root_accts_ = root_accounts;
	}

	/**
	 * XML support only.
	 * @param file_name
	 * @return Number of accounts imported
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws XPathExpressionException 
	 * @throws XPathExpressionException 
	 */
	public int importAccts(String file_name) throws SAXException, IOException {
		if (file_name.endsWith(".xml")) {
			DOMParser parser = new DOMParser();
			parser.parse(file_name);
			Document doc = parser.getDocument();

			HashSet<FileItem> category_items = new HashSet<FileItem>();
			NodeList category_nodes = doc.getElementsByTagName("CATEGORY");
			if (category_nodes != null) {
				for (int i = 0; i < category_nodes.getLength(); i++) {
					Node category_node = category_nodes.item(i);
					category_items.add(this.translateCategory(category_node));
				}
			}

			HashSet<FileItem> acct_items = new HashSet<FileItem>();
			NodeList acct_nodes = doc.getElementsByTagName("ACCOUNT");
			if (acct_nodes != null) {
				for (int i = 0; i < acct_nodes.getLength(); i++) {
					Node acct_node = acct_nodes.item(i);
					acct_items.add(this.translateAccountFileItem(acct_node));
				}
			}

			Hashtable<String, HashSet<AcctActionCategory>> categories = importCategories(category_items);
			return importAccounts(acct_items, categories);
		}
		return 0;
	}

	/**
	 * Obtain a map of categories (it is possible to have multiple categories with same name if they belong in different accounts).
	 * where each AcctActionCategory will have the proper parent set.
	 * @param items FileItem objects corresponding to AcctActionCategory objects in file form.
	 * @return table key: category name. value: list of AcctActionCategory which share the same name
	 */
	private Hashtable<String, HashSet<AcctActionCategory>> importCategories(Collection<FileItem> items) {
		/*key: category name. value: bucket of categories which share the same name, nodes with same owner accounts are considered equal*/
		Hashtable<String, HashSet<AcctActionCategory>> categories = new Hashtable<String, HashSet<AcctActionCategory>>();

		/*key: category name. value: bucket of results which share the same name, nodes with same owner accounts are considered equal*/
		Hashtable<String, HashSet<ItemRegisterResult>> register_results = new Hashtable<String, HashSet<ItemRegisterResult>>();
		int total_unregistered = items.size();
		int previous_total = -1;
		while (total_unregistered > 0 && previous_total != total_unregistered) {
			previous_total = total_unregistered;

			for (FileItem curr_category_item : items) {
				HashSet<ItemRegisterResult> potential_results = register_results.get(curr_category_item
						.getName());

				if (potential_results == null) {
					potential_results = new HashSet<ItemRegisterResult>();
					register_results.put(curr_category_item.getName(), potential_results);
				}

				/*the corresponding result will be one with the same node name, and with the same owner accounts*/
				ItemRegisterResult result = null;
				for (ItemRegisterResult r : potential_results) {
					if (r.getItem().equals(curr_category_item)) {
						result = r;
						break;
					}
				}
				if (result == null || !result.isRegistered()) {
					result = buildCategory(curr_category_item, categories);
					/*ensure one unique entry (with respect to name and owner accounts)*/
					potential_results.remove(result);
					potential_results.add(result);
					if (result.isRegistered())
						total_unregistered--;
				}
			}
		}

		return categories;
	}

	/**
	 * Attempt to build one category, and if applicable obtain a reference to the parent from existing <code>categories</code> table.
	 * @param xml_category
	 * @param xml_category_owners pre-processing requires this to be parsed, so re-use it.
	 * @param categories Category names are only unique per account, so use an array list to handle potential overlapping category names.
	 * 										This will hold all categories in a "non" tree structure, so that account parsing can just go through each one
	 * 										and if it is an owner account, add the category and remove from list.
	 * @return
	 */
	private ItemRegisterResult buildCategory(FileItem item,
			Hashtable<String, HashSet<AcctActionCategory>> categories) {

		ItemRegisterResult result = new ItemRegisterResult(item);
		AcctActionCategory parent = null;
		if (item.getParent() != null) {
			/*the correct parent must have all the owner accounts this category has*/
			HashSet<AcctActionCategory> potential_parents = categories.get(item.getParent());
			if (potential_parents != null) {
				for (AcctActionCategory p_parent : potential_parents) {
					if (item.getOwners().containsAll(p_parent.getOwnerAccts())) {
						parent = p_parent;
						break;
					}
				}
			}
			if (parent == null)
				result.addDependentItem(item.getParent());
		}

		/*if the parent is available to reference, then this category can be constructed*/
		if (result.getDependentNames().size() <= 0) {
			TransactionType norm_bal = TransactionType.valueOf(item.getNormalBalance());
			if (norm_bal == null)
				return result;

			HashSet<AcctActionCategory> corresponding_set = categories.get(item.getName());
			if (corresponding_set == null) {
				corresponding_set = new HashSet<AcctActionCategory>();
				categories.put(item.getName(), corresponding_set);
			}
			AcctActionCategory category = new AcctActionCategory(item.getName(), parent, norm_bal, item
					.getBalance());
			for (String owner_acct : item.getOwners()) {
				category.addOwnerAcct(owner_acct);
			}
			corresponding_set.remove(category);/*ensure this category (uniquely identified by name,owner accounts,children) is only included once*/
			corresponding_set.add(category);
			result.setRegistered();
		}

		return result;
	}

	/**
	 * Attempt to create and register Account objects corresponding to the collection of FileItems passed in.  Also add corresponding
	 * AcctActionCategory objects from the collection <code>categories</code> passed as parameter.
	 * @param items FileItem objects corresponding to Accounts stored on disk.
	 * @param categories Categories previous parsed.
	 * @return Number of accounts that were registered
	 */
	private int importAccounts(Collection<FileItem> items,
			Hashtable<String, HashSet<AcctActionCategory>> categories) {
		Hashtable<String, ItemRegisterResult> register_results = new Hashtable<String, ItemRegisterResult>();
		int total_unregistered = items.size();/*initially all nodes are assumed unregistered*/
		int previous_total = -1;
		/*attempt to register all accounts that have not been registered
		 * if account can not be registered due to account dependencies, it will be attempted on the next iteration*/
		while (total_unregistered > 0 && previous_total != total_unregistered) {
			previous_total = total_unregistered;

			for (FileItem curr_acct_item : items) {
				ItemRegisterResult previous_result = register_results.get(curr_acct_item.getName());
				if (previous_result == null || !previous_result.isRegistered()) {
					ItemRegisterResult result = registerAcct(curr_acct_item, categories);
					register_results.put(result.getItemName(), result);
					if (result.isRegistered()) {
						total_unregistered--;
					}
				}
			}
		}
		return (items.size() - total_unregistered);
	}

	/**
	 * Register an individual FileItem corresponding to an Account, and add corresponding AcctActionCategory objects.
	 * @param acct_item
	 * @param categories
	 * @return
	 */
	private ItemRegisterResult registerAcct(FileItem acct_item,
			Hashtable<String, HashSet<AcctActionCategory>> categories) {
		ItemRegisterResult result = new ItemRegisterResult(acct_item);

		try {
			Account acct = null;
			Account parent = null;
			if (acct_item.getParent() != null) {
				parent = FinanceManager.instance().getAccountManager().getAccount(acct_item.getParent());
				if (parent == null)
					result.addDependentItem(acct_item.getParent());
			}

			AcctType a_type = AcctType.valueOf(acct_item.getType());
			TransactionType n_bal = TransactionType.valueOf(acct_item.getNormalBalance());
			AcctTimeScale t_scale = null;
			if (acct_item.getTimeScale() != null)
				t_scale = AcctTimeScale.valueOf(acct_item.getTimeScale());
			CashCategory cash_category = null;
			if (acct_item.getCashCategory() != null)
				cash_category = CashCategory.valueOf(acct_item.getCashCategory());
			if (a_type == null || n_bal == null)
				return result;

			/*additional processing needed if this is a temporary account*/
			if (AcctType.isTemporaryType(a_type) || !isNullOrEmpty(acct_item.getCloseToAcct())
					|| !isNullOrEmpty(acct_item.getCloseToCategory())
					|| !isNullOrEmpty(acct_item.getCloseFromCategory())
					|| !isNullOrEmpty(acct_item.getCloseToJournal())) {
				Account cta = null;
				Journal ctj = null;
				AcctActionCategory ctc = null;
				AcctActionCategory cfc = null;

				if (!isNullOrEmpty(acct_item.getCloseToAcct())) {/*if close to acct is not yet registered, return this account and attempt to register later*/
					cta = FinanceManager.instance().getAccountManager()
							.getAccount(acct_item.getCloseToAcct());
					if (cta == null)
						result.addDependentItem(acct_item.getCloseToAcct());
				}
				if (result.getDependentNames().size() <= 0) {
					if (!isNullOrEmpty(acct_item.getCloseToJournal()))
						ctj = FinanceManager.instance().getJournalManager().getJournal(
								acct_item.getCloseToJournal());
					if (!isNullOrEmpty(acct_item.getCloseToCategory()) && cta != null) {
						ctc = cta.getActionCategory(acct_item.getCloseToCategory());
					}
					if (!isNullOrEmpty(acct_item.getCloseFromCategory()) && categories != null) {
						/*because this account has not yet been created/registered, the category must be obtained from the table of categories*/
						HashSet<AcctActionCategory> potential_categories = categories.get(acct_item
								.getCloseFromCategory());
						for (AcctActionCategory p_category : potential_categories) {
							if (p_category.getOwnerAccts().contains(acct_item.getName())) {
								cfc = p_category;
								break;/*TODO: remove category from list?*/
							}
						}
					}

					acct = new TemporaryAccount(acct_item.getId(), acct_item.getName(), parent, acct_item
							.getBalance(), cta, a_type, n_bal, ctj, ctc, cfc, cash_category);
				}
			} else {
				if (result.getDependentNames().size() <= 0)
					acct = new Account(acct_item.getId(), acct_item.getName(), parent, a_type, acct_item
							.getBalance(), n_bal, t_scale, cash_category);
			}
			if (result.getDependentNames().size() <= 0) {
				Account existing_acct = FinanceManager.instance().getAccountManager().getAccount(
						acct.getName());

				if (existing_acct == null || existing_acct.lock()) {
					int category_added = 0;

					Account add_cats_to = (existing_acct == null) ? acct : existing_acct;
					/*find corresponding categories, and add them to this account. done before registering, so account will be committed automatically after*/
					for (HashSet<AcctActionCategory> curr_list : categories.values()) {
						for (AcctActionCategory category : curr_list) {
							if (category.getOwnerAccts().contains(acct_item.getName())) {
								/*add the categories from earliest dependent (first to be owned by this acct) to latest (category)
								 * to preserve category tree structure*/
								Stack<AcctActionCategory> cat_dependent_chain = new Stack<AcctActionCategory>();
								AcctActionCategory curr_cat = category;

								do {
									cat_dependent_chain.add(curr_cat);
									curr_cat = curr_cat.getParent();
								} while (curr_cat != null && curr_cat.getOwnerAccts().contains(acct_item.getName()));

								while (cat_dependent_chain.size() > 0) {
									curr_cat = cat_dependent_chain.pop();
									try {
										if (add_cats_to.addActionCategory(curr_cat))
											category_added++;
									} catch (NameAlreadyExistsException e) {
									}
								}
								break;/*category names are unique for each account*/
							}
						}
					}

					if (existing_acct == null)
						FinanceManager.instance().getAccountManager().registerAccount(acct);
					else {
						if (category_added > 0) {
							existing_acct.commit();
						}
						existing_acct.unlock();
					}
				}

				result.setRegistered();
			}
		} catch (NameAlreadyExistsException e) {
			// TODO Auto-generated catch block
			result.setRegistered();/*account is already registered, and should not be re-attempted*/
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * XML support.  Take an XML node and translate to a FileItem object.
	 * @param xml_category
	 * @return
	 */
	private FileItem translateCategory(Node xml_category) {
		HashSet<String> owners = new HashSet<String>();
		String name = null, parent_name = null, norm_b = null;
		double bal = 0;
		for (int x = 0; x < xml_category.getChildNodes().getLength(); x++) {
			Node curr_node = xml_category.getChildNodes().item(x);
			if (curr_node.getNodeName() == "NAME")
				name = curr_node.getTextContent();
			else if (curr_node.getNodeName() == "PARENT")
				parent_name = curr_node.getTextContent();
			else if (curr_node.getNodeName() == "BALANCE") {
				try {
					bal = Double.parseDouble(curr_node.getTextContent());
				} catch (NumberFormatException e) {
					curr_node.setTextContent("0");
				}
			} else if (curr_node.getNodeName() == "NORMALBALANCE")
				norm_b = curr_node.getTextContent();
			else if (curr_node.getNodeName() == "OWNERACCOUNTS") {
				for (int i = 0; i < curr_node.getChildNodes().getLength(); i++) {
					Node owner_node = curr_node.getChildNodes().item(i);
					if (owner_node.getNodeName() == "OWNERACCOUNT") {
						owners.add(owner_node.getTextContent());
					}
				}
			}
		}
		return new FileItem(name, 0, null, norm_b, bal, parent_name, null, null, null, null, null,
				owners, null);
	}

	/**
	 * XML node to Account FileItem.
	 * @param xml_acct
	 * @return
	 */
	private FileItem translateAccountFileItem(Node xml_acct) {
		Node id_node = xml_acct.getAttributes().getNamedItem("ID");
		Integer id = null;
		if (id_node != null) {
			try {
				id = Integer.parseInt(id_node.getTextContent());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

		String name = null, parent_name = null, type = null, time_scale = null, norm_bal = null, cash_category = null, close_to_acct = null, close_to_cat = null, close_from_cat = null, close_to_journal = null;
		double bal = 0;
		for (int i = 0; i < xml_acct.getChildNodes().getLength(); i++) {
			Node curr_child = xml_acct.getChildNodes().item(i);

			if (curr_child.getNodeName() == "NAME")
				name = curr_child.getTextContent();
			else if (curr_child.getNodeName() == "PARENT")
				parent_name = curr_child.getTextContent();
			else if (curr_child.getNodeName() == "TYPE")
				type = curr_child.getTextContent();
			else if (curr_child.getNodeName() == "TIMESCALE")
				time_scale = curr_child.getTextContent();
			else if (curr_child.getNodeName() == "NORMALBALANCE")
				norm_bal = curr_child.getTextContent();
			else if (curr_child.getNodeName() == "CASHCATEGORY")
				cash_category = curr_child.getTextContent();
			else if (curr_child.getNodeName() == "BALANCE") {
				try {
					bal = Double.parseDouble(curr_child.getTextContent());
				} catch (NumberFormatException e) {
					curr_child.setTextContent("0");
				}
			} else if (curr_child.getNodeName() == "CLOSETOACCT")
				close_to_acct = curr_child.getTextContent();
			else if (curr_child.getNodeName() == "CLOSETOCAT")
				close_to_cat = curr_child.getTextContent();
			else if (curr_child.getNodeName() == "CLOSEFROMCAT")
				close_from_cat = curr_child.getTextContent();
			else if (curr_child.getNodeName() == "CLOSETOJOURNAL")
				close_to_journal = curr_child.getTextContent();
		}
		return new FileItem(name, id, type, norm_bal, bal, parent_name, close_to_acct, close_from_cat,
				close_to_cat, close_to_journal, time_scale, null, cash_category);
	}

	private boolean isNullOrEmpty(String s) {
		return (s == null || s.isEmpty());
	}

	/**
	 * DTD will determine valid XML file format for exporting.
	 * @param file_name
	 * @throws IOException
	 */
	private void writeDTD(String file_name) throws IOException {
		String dtd = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
				+ "<!-- Account Definition -->"
				+ "<!ELEMENT ACCOUNTMANAGER (ACCOUNTS,CATEGORIES)>\n"
				+ "<!ELEMENT ACCOUNTS (ACCOUNT*)>\n"
				+ "<!ELEMENT ACCOUNT (NAME,PARENT?,TYPE,TIMESCALE,NORMALBALANCE,BALANCE?,CASHCATEGORY?,CLOSETOCAT?,CLOSEFROMCAT?,CLOSETOACCT?,CLOSETOJOURNAL?)>\n"
				+ "<!ELEMENT NAME (#PCDATA)>\n" + "<!ELEMENT PARENT (#PCDATA)>\n"
				+ "<!ELEMENT TYPE (#PCDATA)>\n" + "<!ELEMENT TIMESCALE (#PCDATA)>\n"
				+ "<!ELEMENT NORMALBALANCE (#PCDATA)>\n" + "<!ELEMENT BALANCE (#PCDATA)>\n"
				+ "<!ELEMENT CLOSETOCAT (#PCDATA)>\n" + "<!ELEMENT CLOSEFROMCAT (#PCDATA)>\n"
				+ "<!ELEMENT CLOSETOJOURNAL (#PCDATA)>\n" + "<!ELEMENT CLOSETOACCT (#PCDATA)>\n"
				+ "<!ELEMENT CASHCATEGORY (#PCDATA)>\n" + "<!ELEMENT CATEGORIES (CATEGORY*)>\n"
				+ "<!ELEMENT CATEGORY (NAME,PARENT?,NORMALBALANCE,BALANCE,OWNERACCOUNTS,OWNERACCOUNT)>\n"
				+ "<!ELEMENT OWNERACCOUNTS (OWNERACCOUNT+)>\n" + "<!ELEMENT OWNERACCOUNT (#PCDATA)>\n"
				+ "<!ATTLIST ACCOUNT ID CDATA #IMPLIED>";

		FileWriter outFile = new FileWriter(file_name);
		PrintWriter out = new PrintWriter(outFile, true);
		out.println(dtd);
		out.close();
	}

	/**
	 * XML support only.
	 * @param file_name
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void exportAccts(String file_name) throws IOException, SAXException {
		if (file_name.endsWith(".xml")) {
			final String dtd_file = file_name.replace(".xml", ".dtd");
			writeDTD(dtd_file);

			FileOutputStream fos = new FileOutputStream(file_name);
			// XERCES 1 or 2 additional classes.
			OutputFormat of = new OutputFormat("XML", "ISO-8859-1", true);
			of.setIndent(1);
			of.setIndenting(true);
			of.setDoctype(null, dtd_file);
			XMLSerializer serializer = new XMLSerializer(fos, of);
			// SAX2.0 ContentHandler.
			org.xml.sax.ContentHandler hd = serializer.asContentHandler();
			hd.startDocument();
			AttributesImpl atts = new AttributesImpl();
			hd.startElement("", "", "ACCOUNTMANAGER", atts);
			/*root accounts can be obtained by finance manager, but root categories are only known about
			 * as they are seen in the account tree traversal*/
			HashSet<AcctActionCategory> acct_root_categories = new HashSet<AcctActionCategory>();
			hd.startElement("", "", "ACCOUNTS", atts);
			for (Account root_acct : this.root_accts_.values()) {
				HashSet<AcctActionCategory> curr_root_categories = createXmlTree(root_acct, hd, atts);
				acct_root_categories.addAll(curr_root_categories);
			}
			hd.endElement("", "", "ACCOUNTS");

			HashSet<AcctActionCategory> exported_cats = new HashSet<AcctActionCategory>();
			hd.startElement("", "", "CATEGORIES", atts);
			for (AcctActionCategory root_category : acct_root_categories) {
				//TODO: ensure that shared categories are only exported once
				createCatXmlTree(root_category, hd, atts, exported_cats);
			}
			hd.endElement("", "", "CATEGORIES");

			hd.endElement("", "", "ACCOUNTMANAGER");
			hd.endDocument();
			fos.close();
		}
	}

	/**
	 * Export all child Account objects under <code>root_node</code>.
	 * @param root_node
	 * @param hd
	 * @param atts
	 * @return A collection of associated AcctActionCategory objects found while par
	 * @throws SAXException
	 */
	private HashSet<AcctActionCategory> createXmlTree(Account root_node,
			org.xml.sax.ContentHandler hd, AttributesImpl atts) throws SAXException {
		/*not allow any duplicate entries in the collection*/
		HashSet<AcctActionCategory> acct_root_categories = new HashSet<AcctActionCategory>();
		Queue<Account> acct_q = new LinkedList<Account>();
		acct_q.add(root_node);

		while (acct_q.size() > 0) {
			Account curr_acct = acct_q.poll();

			createAcctXmlNode(curr_acct, hd, atts);

			acct_root_categories.addAll(curr_acct.getRootActionCategories().values());

			for (Account child_acct : curr_acct.getChildren().values()) {
				acct_q.add(child_acct);
			}
		}
		return acct_root_categories;
	}

	/**
	 * XML export format for one Account.
	 * @param acct
	 * @param hd
	 * @param atts
	 * @throws SAXException
	 */
	private void createAcctXmlNode(Account acct, org.xml.sax.ContentHandler hd, AttributesImpl atts)
			throws SAXException {
		atts.clear();
		atts.addAttribute("", "", "ID", "CDATA", Integer.toString(acct.getAcctId()));
		hd.startElement("", "", "ACCOUNT", atts);

		atts.clear();
		hd.startElement("", "", "NAME", atts);
		hd.characters(acct.getName().toCharArray(), 0, acct.getName().length());
		hd.endElement("", "", "NAME");

		if (acct.getParent() != null) {
			hd.startElement("", "", "PARENT", atts);
			hd.characters(acct.getParent().getName().toCharArray(), 0, acct.getParent().getName()
					.length());
			hd.endElement("", "", "PARENT");
		}

		hd.startElement("", "", "TYPE", atts);
		hd.characters(acct.getAcctType().toString().toCharArray(), 0, acct.getAcctType().toString()
				.length());
		hd.endElement("", "", "TYPE");

		hd.startElement("", "", "TIMESCALE", atts);
		hd.characters(acct.getTimeScale().toString().toCharArray(), 0, acct.getTimeScale().toString()
				.length());
		hd.endElement("", "", "TIMESCALE");

		hd.startElement("", "", "NORMALBALANCE", atts);
		hd.characters(acct.getBalanceSystem().getNormalBalance().toString().toCharArray(), 0, acct
				.getBalanceSystem().getNormalBalance().toString().length());
		hd.endElement("", "", "NORMALBALANCE");

		hd.startElement("", "", "BALANCE", atts);
		hd.characters(Double.toString(acct.getBalanceSystem().getBalance()).toCharArray(), 0, Double
				.toString(acct.getBalanceSystem().getBalance()).length());
		hd.endElement("", "", "BALANCE");

		if (acct instanceof TemporaryAccount) {
			TemporaryAccount temp_acct = (TemporaryAccount) acct;

			hd.startElement("", "", "CLOSETOACCT", atts);
			if (temp_acct.getCloseToAccount() != null) {
				hd.characters(temp_acct.getCloseToAccount().getName().toCharArray(), 0, temp_acct
						.getCloseToAccount().getName().length());
			}
			hd.endElement("", "", "CLOSETOACCT");

			hd.startElement("", "", "CLOSETOCAT", atts);
			if (temp_acct.getCloseToCategory() != null) {
				hd.characters(temp_acct.getCloseToCategory().getName().toCharArray(), 0, temp_acct
						.getCloseToCategory().getName().length());
			}
			hd.endElement("", "", "CLOSETOCAT");

			hd.startElement("", "", "CLOSEFROMCAT", atts);
			if (temp_acct.getCloseFromCategory() != null) {
				hd.characters(temp_acct.getCloseFromCategory().getName().toCharArray(), 0, temp_acct
						.getCloseFromCategory().getName().length());
			}
			hd.endElement("", "", "CLOSEFROMCAT");

			hd.startElement("", "", "CLOSETOJOURNAL", atts);
			if (temp_acct.getCloseToJournal() != null) {
				hd.characters(temp_acct.getCloseToJournal().getName().toCharArray(), 0, temp_acct
						.getCloseToJournal().getName().length());
			}
			hd.endElement("", "", "CLOSETOJOURNAL");
		}

		hd.endElement("", "", "ACCOUNT");
	}

	/**
	 * Export <code>root_cat</code> object and all children AcctActionCategory objects to XML.
	 * @param root_cat
	 * @param hd
	 * @param atts
	 * @return The categories which were exported
	 * @throws SAXException
	 */
	private HashSet<AcctActionCategory> createCatXmlTree(AcctActionCategory root_cat,
			org.xml.sax.ContentHandler hd, AttributesImpl atts, HashSet<AcctActionCategory> exported_cats)
			throws SAXException {
		Queue<AcctActionCategory> cat_q = new LinkedList<AcctActionCategory>();
		cat_q.add(root_cat);

		while (cat_q.size() > 0) {
			AcctActionCategory cat = cat_q.poll();

			if (!exported_cats.contains(cat)) {
				createCatXmlNode(cat, hd, atts);
				exported_cats.add(cat);
			}

			for (AcctActionCategory child_cat : cat.getChildren().values()) {
				cat_q.add(child_cat);
			}
		}
		return exported_cats;
	}

	/**
	 * Create XML nodes corresponding to <code>cat</code>
	 * @param cat
	 * @param hd
	 * @param atts
	 * @throws SAXException
	 */
	private void createCatXmlNode(AcctActionCategory cat, org.xml.sax.ContentHandler hd,
			AttributesImpl atts) throws SAXException {
		hd.startElement("", "", "CATEGORY", atts);

		hd.startElement("", "", "NAME", atts);
		hd.characters(cat.getName().toCharArray(), 0, cat.getName().length());
		hd.endElement("", "", "NAME");

		if (cat.getParent() != null) {
			hd.startElement("", "", "PARENT", atts);
			hd.characters(cat.getParent().getName().toCharArray(), 0, cat.getParent().getName().length());
			hd.endElement("", "", "PARENT");
		}

		hd.startElement("", "", "NORMALBALANCE", atts);
		hd.characters(cat.getBalanceSystem().getNormalBalance().toString().toCharArray(), 0, cat
				.getBalanceSystem().getNormalBalance().toString().length());
		hd.endElement("", "", "NORMALBALANCE");

		hd.startElement("", "", "BALANCE", atts);
		hd.characters(Double.toString(cat.getBalanceSystem().getBalance()).toCharArray(), 0, Double
				.toString(cat.getBalanceSystem().getBalance()).length());
		hd.endElement("", "", "BALANCE");

		/*when reading an account, how to know which parent (possible same names for categories under different accounts)
		 *-all of parent category owner accounts must be in the child category owner accounts*/
		hd.startElement("", "", "OWNERACCOUNTS", atts);
		for (String owner : cat.getOwnerAccts()) {
			hd.startElement("", "", "OWNERACCOUNT", atts);
			hd.characters(owner.toCharArray(), 0, owner.length());
			hd.endElement("", "", "OWNERACCOUNT");
		}
		hd.endElement("", "", "OWNERACCOUNTS");

		hd.endElement("", "", "CATEGORY");
	}
}
