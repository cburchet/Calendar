package server;
import java.rmi.RemoteException;
import java.rmi.Remote;
import java.util.*;

public interface CalendarRemoteInterface extends Remote
{
  public static ArrayList<CalendarObject> calendarObjects = new ArrayList<CalendarObject>();
  public static ArrayList<String> users = new ArrayList<String>();
  //make a calendar object
  public String MakeCalendar(String userName) throws RemoteException;
  //return access to the calendar
  public EventInterface GetAccessToCalendar(String name) throws RemoteException;
  //return names of all users
  public String[] GetAllUsers() throws RemoteException;
  //return all calendar objects
  public ArrayList<CalendarObject> GetAllCalendars() throws RemoteException;
}