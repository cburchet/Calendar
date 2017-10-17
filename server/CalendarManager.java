package server; 
import java.rmi.*;
import java.util.Date;
import java.util.ArrayList;
import java.rmi.server.UnicastRemoteObject;

//added synchronized keyword to functions todo: test before midnight
public class CalendarManager extends UnicastRemoteObject implements CalendarRemoteInterface
{
  
  public static void main(String[] args)
  {
    try
    {
      System.setSecurityManager(new SecurityManager());
      CalendarManager calendarManager = new CalendarManager();
      Naming.rebind("rmi://localhost:65380/CalendarManager", calendarManager);
    }
    catch(Exception e)
    {
      System.out.println(e);
    }
    System.out.println("Server Ready!");
  }
  
  public ArrayList<CalendarObject> GetAllCalendars() throws RemoteException
  {
    return calendarObjects;
  }
  
  public synchronized String MakeCalendar(String userName) throws RemoteException
  {
    //check that userName does not already have a calendar
    boolean wasFound = users.contains(userName);
    //create the calendar and add it to the list if not
    if (!wasFound)
    {
      CalendarObject newCalendar = new CalendarObject(userName);
      calendarObjects.add(newCalendar);
      users.add(userName);
      return userName;
    }
    return userName + " was already created";
  }
  
  //find the calendar and return it
  public synchronized EventInterface GetAccessToCalendar(String name) throws RemoteException
  {
    int eventIndex = users.indexOf(name);
    if (eventIndex < 0)
    {
      return null;
    }
    else
    {
      CalendarObject eventRequested = (CalendarObject)calendarObjects.get(eventIndex);
      return (EventInterface)eventRequested;
    }
  }
  //return a string[] representing all users
  public synchronized String[] GetAllUsers() throws RemoteException
  {  
    String[] temp = users.toArray(new String[users.size()]);
    return temp;
  }
  
  public CalendarManager() throws RemoteException
  {}
}