package reman.common.database;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * This class exists so there is one place where each managed class's field list can be  
 * persisted into the database.
 * @author Scott
 *
 */
public class ManagedClassRecord extends DatabaseObject {
	private ArrayList<String> managed_field_names_;
	private String managed_class_name_;

	/*generated on dynamically based of class/field names*/
	private transient ArrayList<Field> managed_fields_;
	private transient Class<? extends DatabaseObject> managed_class_;

	/**
	 * Pass the name of a class that inherits from DatabaseObject
	 * @param managed_class_name 
	 */
	public ManagedClassRecord(Class<? extends DatabaseObject> dbo_class) {
		this.managed_class_name_ = dbo_class.getName();
		this.managed_class_ = dbo_class;
		this.managed_field_names_ = new ArrayList<String>();
		this.managed_fields_ = new ArrayList<Field>();
	}

	/**
	 * Used to check whether this record is still managing a class.
	 * @return
	 */
	public boolean isValidRecord() {
		return this.managed_class_name_ != null;
	}

	/**
	 * Add a field to be managed, if valid in this managed class and not already not managed.
	 * @param f
	 * @return True if field was added to the managed list.
	 */
	public boolean addManagedField(Field f) {
		if (f == null)
			return false;
		try {
			if (this.getManagedClass() != null
					&& this.managed_class_.getDeclaredField(f.getName()) == null)
				return false;
		} catch (SecurityException e) {
			/*this is fine, security will be set later.*/
		} catch (NoSuchFieldException e) {
			return false;/*this was the point, if the field is not part of the managed class, don't manage it*/
		}
		if (!this.managed_field_names_.contains(f.getName())) {
			boolean added = this.managed_fields_.add(f);
			return added && this.managed_field_names_.add(f.getName());
		}
		return false;
	}

	/**
	 * This is needed because the managed_class_ can not be persisted to the database, and it must be re-generated every run.
	 * @return
	 * @throws IllegalArgumentException
	 * @throws ClassNotFoundException
	 */
	private Class<? extends DatabaseObject> getManagedClass() {
		if (this.managed_class_ == null) {
			if (this.managed_class_name_ == null)
				return null;

			Class managed_class;
			try {
				managed_class = Class.forName(this.managed_class_name_);
			} catch (ClassNotFoundException e) {
				System.err
						.println("Managed class '" + this.managed_class_name_ + "' is not a valid class.");
				e.printStackTrace();
				return null;
			}
			if (DatabaseManager.isDatabaseObject(managed_class))
				this.managed_class_ = (Class<? extends DatabaseObject>) managed_class;
		}
		return this.managed_class_;
	}

	/**
	 * This is needed because the managed_fields_ can not be persisted to the database, and must be re-generated every run.
	 * @return
	 */
	public ArrayList<Field> getManagedFields() {
		if (this.managed_fields_ != null && this.managed_fields_.size() == this.managed_field_names_.size())
			return this.managed_fields_;
		try {
			this.managed_fields_ = new ArrayList<Field>(this.managed_field_names_.size());
			Class<? extends DatabaseObject> managed_class = this.getManagedClass();
			if (managed_class != null) {
				for (String field_name : this.managed_field_names_) {
					Field f;
					try {
						f = managed_class.getDeclaredField(field_name);
						f.setAccessible(true);
						this.managed_fields_.add(f);
					} catch (NoSuchFieldException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} catch (IllegalArgumentException e) {
			// TODO This won't happen, class is checked upon setting the class name
			e.printStackTrace();
		} catch (SecurityException e) {
			/*don't think this will happen*/
			e.printStackTrace();
		}
		return this.managed_fields_;
	}

	/**
	 * Classes to remove a field that is no longer needs to be managed, or if invalid field encountered.
	 * @param f
	 * @return
	 */
	public boolean removeMangedField(Field f) {
		boolean removed = true;
		if (this.managed_fields_ != null)
			removed = this.managed_fields_.remove(f);

		return removed && this.managed_field_names_.remove(f.getName());
	}
}
