package reman.client.app.notepad;

import reman.client.app.UserDatabaseObject;

/**
 * A Note is a simple object with text.  Notes are helpful
 * informal reminders.  Notes are personal and therefore changes
 * are not broadcasted.  
 * @author jonathan
 *
 */
public class Note extends UserDatabaseObject {
  private String text_;

  public Note() {

  }

  public Note(String name) {
    this.setName(name);
  }

  /**
   * This function updates the text value of the note
   * @param text - the new text for the note
   */
  public void setNoteText(String text) {
    text_ = text;
  }

  /**
   * Retrieves the text value for the note
   * @return String with the text for the note
   */
  public String getNoteText() {
    return text_;
  }
}
