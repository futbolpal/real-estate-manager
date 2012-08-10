package reman.client.app.finance.timer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;

import reman.common.database.DatabaseObject;
import reman.common.database.OfficeProjectManager;
import reman.common.database.exceptions.DatabaseException;
import reman.common.database.exceptions.LoggedInException;
import reman.common.database.exceptions.NotPersistedException;

public class TimeEventManager extends DatabaseObject implements Runnable {

	private ArrayList<TimeNotificationEntry> list_;

	public TimeEventManager() {
		list_ = new ArrayList<TimeNotificationEntry>();
	}

	public static Calendar getLastOfYear() {
		Calendar cld = Calendar.getInstance();
		cld.set(Calendar.MONTH, cld.getMaximum(Calendar.MONTH));
		cld.set(Calendar.DATE, cld.getMaximum(Calendar.DATE));
		cld.set(Calendar.MINUTE, cld.getMaximum(Calendar.MINUTE));
		cld.set(Calendar.SECOND, cld.getMaximum(Calendar.SECOND));
		cld.set(Calendar.HOUR_OF_DAY, cld.getMaximum(Calendar.HOUR_OF_DAY));
		cld.set(Calendar.MILLISECOND, cld.getMaximum(Calendar.MILLISECOND));
		return cld;
	}

	@Override
	public void run() {
		/*timer events will automatically fired on already expired events*/
		for (TimeNotificationEntry e : list_) {
			addTimerListener(e);
		}
	}

	public boolean unRegister(TimeNotificationEntry e) throws DatabaseException, SQLException {
		return this.unRegister(e, true);
	}

	boolean unRegister(TimeNotificationEntry e, boolean automate_db) throws DatabaseException,
			SQLException {
		this.retrieve();

		boolean success = false;
		try {
			if (!automate_db || this.lock()) {
				this.removeTimerListener(e);
				success = list_.remove(e);
				if (success && automate_db)
					this.commit();
			}
		} catch (LoggedInException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw e1;
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw e1;
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw e1;
		} finally {
			if (automate_db)
				this.unlock();
		}
		return success;
	}

	public boolean register(TimeNotificationEntry e) throws DatabaseException, SQLException {
		return this.register(e, true);
	}

	boolean register(TimeNotificationEntry e, boolean automate_db) throws DatabaseException,
			SQLException {
		this.retrieve();

		boolean success = false;
		try {
			if (!automate_db || this.lock()) {
				this.addTimerListener(e);
				success = list_.add(e);
				if (success && automate_db)
					this.commit();
			}
		} catch (LoggedInException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw e1;
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw e1;
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw e1;
		} finally {
			if (automate_db)
				this.unlock();
		}
		return success;
	}

	private void addTimerListener(TimeNotificationEntry e) {
		Timer t = new Timer();
		t.schedule(e.getTimerTask(), e.getNotifyTime());
	}

	private void removeTimerListener(TimeNotificationEntry e) {
		e.getTimerTask().cancel();
	}

	void synchManager() throws LoggedInException, SQLException {
		try {
			if (this.lock())
				this.commit();
		} catch (LoggedInException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			this.unlock();
		}
	}

	@Override
	public void retrieve() throws DatabaseException, SQLException {
		try {
			super.retrieve();
		} catch (NotPersistedException e) {
			//ignore exception
		}
	}

	public static TimeEventManager instance() {
		return OfficeProjectManager.instance().getCurrentProject().getTimeEventManager();
	}
}
