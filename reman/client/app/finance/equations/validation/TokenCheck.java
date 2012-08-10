package reman.client.app.finance.equations.validation;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import reman.client.app.finance.equations.exceptions.MathException;
import reman.client.app.finance.equations.exceptions.MathParseException;
import reman.client.app.finance.equations.functions.Function;
import reman.client.app.finance.equations.tokens.CloseParen;
import reman.client.app.finance.equations.tokens.CommaToken;
import reman.client.app.finance.equations.tokens.DoubleToken;
import reman.client.app.finance.equations.tokens.OpenParen;
import reman.client.app.finance.equations.tokens.Operator;
import reman.client.app.finance.equations.tokens.Token;
import reman.client.app.finance.equations.tokens.TokenMarkType;
import reman.client.app.finance.equations.tokens.Variable;

/**
 * design reference: http://www.ibm.com/developerworks/library/j-w3eval/index.html
 * Each equation undergoes a TokenCheck and uses tokenizeExpression.  This will yield a variable list that could be
 * chained to other equations if they are related.
 * @author Scott
 *
 */
public class TokenCheck {

	private boolean[][] valid_combinations_;

	/**
	 * Each new variable will be contained with in this array list, instead of directly in each formula.
	 * Each formula will have a reference to a variable within this list. Indexed by variable name.
	 */
	private Hashtable<String, Variable> variables_;

	private ArrayList<Token> tokens_;

	/**
	 * number parens seen*/
	private int num_parens_;

	public TokenCheck() {
		this.tokens_ = new ArrayList<Token>();
		this.variables_ = new Hashtable<String, Variable>();
		this.num_parens_ = 0;
		this.valid_combinations_ = new boolean[TokenMarkType.values().length][TokenMarkType.values().length];

		this.initValidCombinations();
	}

	public Hashtable<String, Variable> getVariables() {
		return this.variables_;
	}

	public ArrayList<Token> getTokens() {
		return this.tokens_;
	}

	public int getNumParens() {
		return this.num_parens_;
	}

	/**
	 * If successful getTokens will be populated with tokens.
	 * @param expression
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 * @throws MathException 
	 */
	public void tokenizeExpression(String expression) throws MathException {
		/*parenthesis can be checked easily with out tokenizing expression*/
		String errorMessage = "";

		if (ParenthesesCheck.parenthesesValid(expression)) {
			/*remove all white space characters*/
			expression = expression.replaceAll("\\s*", "");
			expression = expression.replaceAll("\\([-]", "(-1*");
			if (expression.startsWith("-("))
				expression = "-1*" + expression.substring(1);
			this.tokens_ = getTokens(expression);

			errorMessage = this.isTokensValid(this.tokens_);
		} else
			errorMessage = "Parentheses error.";
		if (errorMessage != "") {
			this.tokens_ = null;
			this.variables_ = null;
			this.num_parens_ = 0;
			throw new MathException(errorMessage);
		}
	}

	private ArrayList<Token> getTokens(String expression) throws MathException {

		ArrayList<Token> tokens = new ArrayList<Token>();

		Pattern variable_pattern = Pattern.compile("[a-zA-Z]([a-zA-Z]|[0-9])*");

		/*indexed by priority of checking*/
		Hashtable<Integer, Pattern> patterns = new Hashtable<Integer, Pattern>(7);

		patterns.put(TokenMarkType.VARIABLE.getParseCheckPriority(), variable_pattern);/*variable pattern*/
		patterns.put(TokenMarkType.DOUBLE.getParseCheckPriority(), Pattern
				.compile("[\\-]?[0-9]+([.][0-9]*)?"));/*double pattern*/
		patterns.put(TokenMarkType.OPERATOR.getParseCheckPriority(), Pattern
				.compile("[\\+\\-\\*/\\^%]|(((=|!)=)|((>|<)=?))|(\\&\\&|\\|\\||or|and)|(<<|((>>)>?)|xor)"));/*operator paren*/
		patterns.put(TokenMarkType.OPEN_PAREN.getParseCheckPriority(), Pattern.compile("[\\(\\{\\[]"));/*open paren pattern*/
		patterns.put(TokenMarkType.CLOSE_PAREN.getParseCheckPriority(), Pattern.compile("[\\)\\}\\]]"));/*close paren pattern*/
		patterns.put(TokenMarkType.COMMA.getParseCheckPriority(), Pattern.compile("[,]"));/*comma pattern*/
		patterns.put(TokenMarkType.FUNCTION.getParseCheckPriority(), Pattern.compile(variable_pattern
				.pattern()
				+ "(?=\\()"));/*function pattern. '?=(' means look ahead of (*/

		/*to avoid infinite loop, set previous index right as soon as while loop is entered*/
		int previous_input_index = -1;
		int input_index = 0;
		while (input_index < expression.length() && previous_input_index != input_index) {
			previous_input_index = input_index;
			String curr_input = expression.substring(input_index);

			for (int pattern_index = 0; pattern_index < patterns.size(); pattern_index++) {
				Pattern curr_pattern = patterns.get(pattern_index);
				TokenMarkType match_test_type = null;
				/*if we have previously seen a DOUBLE then we need to check for an operator before double
				 * ex: 2-3 -> should parse as double(2) op(-) and double(3) NOT as double(2) double(-3)
				 * if we have previously seen a CLOSE_PAREN then we need to check for an operator before double
				 * ex: (5-4)-2 -> should parse as ( double(5) op(-) double(4) ) op(-) double(2)*/
				if (tokens.size() > 0) {
					TokenMarkType last_token_mark = tokens.get(tokens.size() - 1).getMark();
					if (last_token_mark.equals(TokenMarkType.DOUBLE)
							|| last_token_mark.equals(TokenMarkType.CLOSE_PAREN)) {
						if (TokenMarkType.DOUBLE.getParseCheckPriority() < TokenMarkType.OPERATOR
								.getParseCheckPriority()) {
							if (pattern_index == TokenMarkType.DOUBLE.getParseCheckPriority()) {
								curr_pattern = patterns.get(TokenMarkType.OPERATOR.getParseCheckPriority());
								match_test_type = TokenMarkType.OPERATOR;
							} else if (pattern_index == TokenMarkType.OPERATOR.getParseCheckPriority()) {
								curr_pattern = patterns.get(TokenMarkType.DOUBLE.getParseCheckPriority());
								match_test_type = TokenMarkType.DOUBLE;
							}
						}
					}
				}

				Matcher curr_matcher = curr_pattern.matcher(curr_input);

				if (curr_matcher.lookingAt()) {
					Token curr_token = null;
					String match_text = curr_matcher.group();
					if (match_test_type == null)
						match_test_type = TokenMarkType.getMarkOfPriority(pattern_index);

					if (match_test_type == TokenMarkType.DOUBLE) {
						curr_token = new DoubleToken(Double.parseDouble(match_text));
					} else if (match_test_type == TokenMarkType.VARIABLE) {
						Variable curr_var = null;
						/*if variable is already in table, use it instead of creating an independent reference*/
						curr_var = this.variables_.get(match_text);
						/*variable is not in the table, create it and add it*/
						if (curr_var == null) {
							curr_var = new Variable(match_text);
							this.variables_.put(curr_var.getName(), curr_var);
						}
						curr_token = curr_var;
					} else if (match_test_type == TokenMarkType.OPERATOR) {
						curr_token = new Operator(match_text);
					} else if (match_test_type == TokenMarkType.OPEN_PAREN) {
						curr_token = new OpenParen(match_text);
						this.num_parens_++;
					} else if (match_test_type == TokenMarkType.CLOSE_PAREN) {
						curr_token = new CloseParen(match_text);
						this.num_parens_++;
					} else if (match_test_type == TokenMarkType.FUNCTION) {
						/*dynamically create the type of function that is encountered*/
						String fnt_canonical_name = Function.getCanonicalName(match_text);
						Class fnt_class;
						try {
							fnt_class = Class.forName(fnt_canonical_name);
							curr_token = (Function) fnt_class.newInstance();
						} catch (ClassNotFoundException e) {
							throw new MathException("Class defintion not found for Function '" + match_text
									+ "'.  Expected definition at '" + fnt_canonical_name + "'.");
						} catch (InstantiationException e) {
							throw new MathException("Class instantion exception on Function '" + match_text
									+ "'.");
						} catch (IllegalAccessException e) {
							throw new MathException("Constructor illegal access exception on Function '"
									+ match_text + "'.");
						}
					} else if (match_test_type == TokenMarkType.COMMA) {
						curr_token = new CommaToken();
					} else {
						throw new MathParseException("Unhandled TakenMarkType in TokenCheck.getTokens(..) of '"
								+ match_test_type + "'.", input_index);
					}
					//curr_token = new Token(token, match_test_type, input_index, match_text.length());
					tokens.add(curr_token);

					input_index += curr_matcher.end();

					break;
				}
			}
		}

		/*error unrecognized token found*/
		if (previous_input_index == input_index) {
			throw new MathParseException("Unhandled token around encountered at index '" + input_index
					+ "' character '" + expression.charAt(input_index) + "'.", input_index);
		}

		return tokens;
	}

	/**
	 * 
	 * @param tokens
	 * @return Empty string if valid, or error message
	 */
	private String isTokensValid(ArrayList<Token> tokens) {
		if (tokens.size() <= 0)
			return "";//trivially true

		String errorMessage = "";
		if ((errorMessage = this.isBeginTokensValid(tokens)) != "")
			return errorMessage;
		if ((errorMessage = this.isEndTokensValid(tokens)) != "")
			return errorMessage;
		if ((errorMessage = this.isTokenSequenceValid(tokens)) != "")
			return errorMessage;
		if ((errorMessage = this.isFunctionTokensValid(tokens)) != "")
			return errorMessage;
		if ((errorMessage = this.isCommaTokensValid(tokens)) != "")
			return errorMessage;

		return "";
	}

	/**
	 * 
	 * @param tokens
	 * @return empty string if valid, or error message
	 */
	private String isBeginTokensValid(ArrayList<Token> tokens) {

		String errorMessage = "";
		/*check beginning of token list, can't start with operator, comma, close paren*/
		if (tokens.get(0).getMark() == TokenMarkType.OPERATOR)
			errorMessage = "Expression can not start with an operator";
		else if (tokens.get(0).getMark() == TokenMarkType.COMMA)
			errorMessage = "Expression can not start with a comma";
		else if (tokens.get(0).getMark() == TokenMarkType.CLOSE_PAREN)
			errorMessage = "Expression can not start with a ')'";
		return errorMessage;
	}

	/**
	 * 
	 * @param tokens
	 * @return empty string if valid, or error message
	 */
	private String isEndTokensValid(ArrayList<Token> tokens) {

		String errorMessage = "";

		/*check end of token list, can't end with operator, function, comma, open paren*/
		int last_element_index = tokens.size() - 1;
		if (tokens.get(last_element_index).getMark() == TokenMarkType.OPERATOR)
			errorMessage = "Expression can not end with an operator";
		else if (tokens.get(last_element_index).getMark() == TokenMarkType.COMMA)
			errorMessage = "Expression can not end with a comma";
		else if (tokens.get(last_element_index).getMark() == TokenMarkType.OPEN_PAREN)
			errorMessage = "Expression can not end with a '('";
		else if (tokens.get(last_element_index).getMark() == TokenMarkType.FUNCTION)
			errorMessage = "Expression can not end with a function";

		return errorMessage;
	}

	/**
	 * Uses 'valid_combinations_' to determine if each token is arranged in an acceptable way
	 * @param tokens
	 */
	private String isTokenSequenceValid(ArrayList<Token> tokens) {

		for (int i = 0; i < tokens.size(); i++) {
			Token curr_token = tokens.get(i);
			Token next_token = ((i + 1 < tokens.size()) ? tokens.get(i + 1) : null);

			if (next_token != null) {
				if (!this.valid_combinations_[curr_token.getMark().getSeqCheckArrayIndex()][next_token
						.getMark().getSeqCheckArrayIndex()])
					return "Invalid token sequence. Token type '" + curr_token.getMark() + "' at position '"
							+ i + "' can not be followed by token type '" + next_token.getMark()
							+ "' at position '" + (i + 1) + "'.";
			}
		}
		return "";
	}

	/**
	 * Should check all functions in token stream with expected amount of arguments.  Using -1 as number for arbitrary amount of arguments.
	 * Should be able to accept functions as argument to other functions.
	 * 
	 * @param tokens
	 * @return empty string if valid, or error message
	 */
	private String isFunctionTokensValid(ArrayList<Token> tokens) {

		/*maintain two parallel stacks of current function being checked (top element), and corresponding args seen*/
		Stack<Integer> arg_counts = new Stack<Integer>();
		Stack<Function> functions = new Stack<Function>();

		/*example: fun(fun(2,3),fun(5),x,5)*/

		for (Token t : tokens) {
			if (t.getMark() == TokenMarkType.FUNCTION) {
				Function curr_funct = (Function) t;

				/*if currently in a function then this function is an argument of the current function
				 * so increment current arg count*/
				if (arg_counts.size() > 0 && functions.size() > 0) {
					arg_counts.push(arg_counts.pop().intValue() + 1);
				}

				/*initially no arguments counted for this new function*/
				arg_counts.push(new Integer(0));
				/*expected arguments for this function*/
				functions.push(curr_funct);
			} else if (t.getMark() == TokenMarkType.CLOSE_PAREN) {
				if (arg_counts.size() > 0 && functions.size() > 0) {
					if (functions.peek().getValidNumArgs() > -1
							&& functions.peek().getValidNumArgs() != arg_counts.peek()) {
						return "Function '" + functions.peek().toString() + "' requires '"
								+ functions.peek().getValidNumArgs() + "' arguments but found '"
								+ arg_counts.peek() + "' arguments.";
					} else {
						/*check passed for current function, get rid of current function and arg count*/
						functions.pop();
						arg_counts.pop();
					}
				}
			} else if (t.getMark() != TokenMarkType.COMMA && t.getMark() != TokenMarkType.OPEN_PAREN) {
				/*this is an argument*/
				if (arg_counts.size() > 0 && functions.size() > 0) {
					arg_counts.push(arg_counts.pop().intValue() + 1);
				} else if (arg_counts.size() > 0 && functions.size() <= 0) {
					return "Invalid token stream. Attempted to increase argument count but current function or current count missing.";
				}
			}
		}
		return "";
	}

	/**
	 * Check token stream abides by the rule that commas can only be used to separate function arguments.
	 * Should handle functions as parameters to functions.
	 * @param tokens
	 * @return empty string if valid, or error message
	 */
	private String isCommaTokensValid(ArrayList<Token> tokens) {

		int open_funciton_paren_count = 0;
		boolean in_function = false;

		/*example: fun(fun(2,3),fun(5),x,5)*/
		for (Token t : tokens) {
			if (t.getMark() == TokenMarkType.COMMA) {
				if (!in_function)
					return "Comma can not be used out side a function at position '" + t.getPosition() + "'.";
			} else if (t.getMark() == TokenMarkType.FUNCTION) {
				in_function = true;
			} else if (t.getMark() == TokenMarkType.OPEN_PAREN) {
				if (in_function)
					open_funciton_paren_count++;
			} else if (t.getMark() == TokenMarkType.CLOSE_PAREN) {
				if (in_function) {
					open_funciton_paren_count--;
					if (open_funciton_paren_count == 0)
						in_function = false;
				}
			}
		}

		return "";
	}

	private void initValidCombinations() {
		/*init to all false*/
		for (int row = 0; row < this.valid_combinations_.length; row++) {
			for (int col = 0; col < this.valid_combinations_[0].length; col++) {
				this.valid_combinations_[row][col] = false;
			}
		}

		/*double can be followed by operator, close paren, comma*/
		this.valid_combinations_[TokenMarkType.DOUBLE.getSeqCheckArrayIndex()][TokenMarkType.OPERATOR
				.getSeqCheckArrayIndex()] = true;
		this.valid_combinations_[TokenMarkType.DOUBLE.getSeqCheckArrayIndex()][TokenMarkType.CLOSE_PAREN
				.getSeqCheckArrayIndex()] = true;
		this.valid_combinations_[TokenMarkType.DOUBLE.getSeqCheckArrayIndex()][TokenMarkType.COMMA
				.getSeqCheckArrayIndex()] = true;

		/*variables can be followed by same amounts as double*/
		this.valid_combinations_[TokenMarkType.VARIABLE.getSeqCheckArrayIndex()] = this.valid_combinations_[TokenMarkType.DOUBLE
				.getSeqCheckArrayIndex()];

		/*function can be followed by open paren */
		this.valid_combinations_[TokenMarkType.FUNCTION.getSeqCheckArrayIndex()][TokenMarkType.OPEN_PAREN
				.getSeqCheckArrayIndex()] = true;

		/*operator can be followed by double, variable, function, open paren*/
		this.valid_combinations_[TokenMarkType.OPERATOR.getSeqCheckArrayIndex()][TokenMarkType.DOUBLE
				.getSeqCheckArrayIndex()] = true;
		this.valid_combinations_[TokenMarkType.OPERATOR.getSeqCheckArrayIndex()][TokenMarkType.VARIABLE
				.getSeqCheckArrayIndex()] = true;
		this.valid_combinations_[TokenMarkType.OPERATOR.getSeqCheckArrayIndex()][TokenMarkType.FUNCTION
				.getSeqCheckArrayIndex()] = true;
		this.valid_combinations_[TokenMarkType.OPERATOR.getSeqCheckArrayIndex()][TokenMarkType.OPEN_PAREN
				.getSeqCheckArrayIndex()] = true;

		/*open paren can be followed by same elements as operator*/
		this.valid_combinations_[TokenMarkType.OPEN_PAREN.getSeqCheckArrayIndex()] = this.valid_combinations_[TokenMarkType.OPERATOR
				.getSeqCheckArrayIndex()];

		/*close paren can be followed by same as double*/
		this.valid_combinations_[TokenMarkType.CLOSE_PAREN.getSeqCheckArrayIndex()] = this.valid_combinations_[TokenMarkType.DOUBLE
				.getSeqCheckArrayIndex()];

		/*comma can be followed by same as operator*/
		this.valid_combinations_[TokenMarkType.COMMA.getSeqCheckArrayIndex()] = this.valid_combinations_[TokenMarkType.OPERATOR
				.getSeqCheckArrayIndex()];
	}
}
