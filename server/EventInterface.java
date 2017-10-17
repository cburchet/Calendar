package server;
import java.util.*;
import java.rmi.*;

public interface EventInterface extends Remote
{
  //get calendars events arraylist
  public ArrayList<Event> GetListsOfEvents() throws RemoteException;
  //return an alert when an event is < 15 min away
  public String EventAlert() throws RemoteException;
  //return all events for user in a string[]
  public String[] ReturnAllEventsForUser(String user) throws RemoteException;
  //remove event from user's calendar
  public String RemoveEvent(String user, int event) throws RemoteException;
  //add event to this calendar
  public String AddEvent(String eventName, String eventOwner, String[] eventGuests,
                         Date start, Date end, String eventType) throws RemoteException;
  //find all events within range of two dates
  public String[] RetrieveUsersEventsWithinRange(String user, Date start, Date end) throws RemoteException;
}