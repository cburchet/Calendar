package server;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.text.*;

//added synchronized keyword to functions todo: test before midnight
public class CalendarObject extends UnicastRemoteObject implements EventInterface
{
  public String user = "";
  public ArrayList<Event> calendarEvents;
  public ArrayList<Boolean> wasAlerted;
  public CalendarObject(String _user) throws RemoteException
  {
    user = _user;
    wasAlerted = new ArrayList<Boolean>();
    calendarEvents = new ArrayList<Event>();
    //create a default open event from May 1 2016-2018
    try
    {
      DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy/HH:mm");
      Date startOpen = formatter.parse("05/01/2016/8:00");
      Date endOpen = formatter.parse("05/01/2018/8:00");
      this.AddEvent("starting open event", user, new String[0],
                    startOpen, endOpen, "Open");
    }
    catch(Exception e)
    {
      System.out.println("Creating starting open event failed: " + e);
    }
  }
  
  public synchronized ArrayList<Event> GetListsOfEvents() throws RemoteException
  {
    return calendarEvents;
  }
  //return an alert string if there is an event within fifteen min of current time
  public synchronized String EventAlert() throws RemoteException
  {
    int fifteenMinutes = 1000*60*15;
    String eventAlert = "";
    for (int i = 0; i < calendarEvents.size(); i++)
    {
      //make sure wasAlerted is same size as calendarEvents
      if (wasAlerted.size() == calendarEvents.size())
      {
        //make sure an event only has an alert printed once
        if (wasAlerted.get(i) == false)
        {
          //check that the event starts before 15 min after current time and starts after current time
          Event checkedEvent = calendarEvents.get(i);
          Date currentTime = new Date();
          Date fifteenFromCurrent = new Date(currentTime.getTime() + fifteenMinutes);
          if (checkedEvent.start.before(fifteenFromCurrent) && checkedEvent.start.after(currentTime))
          {
            wasAlerted.set(i, true);
            eventAlert += "Alert!!!! You have a scheduled event at ";
            eventAlert += checkedEvent.start.toString();
            return eventAlert;
          }
        }
      }
    }
    return eventAlert;
  }
  
  public void PrintAlert(String alert) throws RemoteException
  {
    System.out.println(alert);
  }
  
  //return all events for owner or non-private events for non owners
  public synchronized String[] ReturnAllEventsForUser(String _user) throws RemoteException
  {
    
    int userIndex = CalendarManager.users.indexOf(_user);
    if (userIndex < 0)
    {
      System.out.println("user was not found");
      return new String[0];
    }
    CalendarObject userCalendar = CalendarManager.calendarObjects.get(userIndex);
    String[] eventStrings = new String[userCalendar.calendarEvents.size()];
    EventInterface guestCalendar = (EventInterface)userCalendar;
    for (int i = 0; i < userCalendar.calendarEvents.size(); i++)
    {
      Event current = userCalendar.calendarEvents.get(i);
      //user is owner of events show all events
      if (current.eventOwner.equals(user))
      {
        eventStrings[i] = current.eventName +  " " + current.eventOwner + " " + current.start.toString() +
          " " + current.end.toString() + " " + current.eventType;
        for (int j = 0; j < current.eventGuests.length; j++)
        {
          eventStrings[i] += current.eventGuests[j];
        }
      }
      //user is not owner show non private events
      else
      {
        //add the details of all open and public events
        if (!current.eventType.equalsIgnoreCase("Private") && !current.eventType.equalsIgnoreCase("Group"))
        {
          eventStrings[i] = current.eventName + " " + current.eventOwner + " " + current.start.toString() + " " +
            current.end.toString() + " " + current.eventType;
          for (int j = 0; j < current.eventGuests.length; j++)
          {
            eventStrings[i] += current.eventGuests[j];
          }
        }
        //show that user has a private/group event here but no details
        else
        {
          eventStrings[i] = "private/Group event";
        }
      }
    }
    return eventStrings;
  }
  
  public synchronized String RemoveEvent(String user, int event) throws RemoteException
  {
    int userIndex = CalendarManager.users.indexOf(user);
    CalendarObject userCalendar = CalendarManager.calendarObjects.get(userIndex);
    Event eventToRemove = userCalendar.calendarEvents.get(event);
    //delete group event (Note: only owner can delete it)
    if (eventToRemove.eventType.equalsIgnoreCase("Group"))
    {
      if (eventToRemove.eventOwner.equals(user))
      {
        String[] guests = eventToRemove.eventGuests;
        for (int i = 0; i < guests.length; i++)
        {
          //get the guest's calendar
          CalendarObject guestCalendar = CalendarManager.calendarObjects.get(CalendarManager.users.indexOf(guests[i]));
          Event checkIfEvent;
          //find the correct group event and remove it
          for (int j = 0; j < guestCalendar.GetListsOfEvents().size(); j++)
          {
            checkIfEvent = guestCalendar.GetListsOfEvents().get(j);
            if (checkIfEvent.start.equals(eventToRemove.start) && checkIfEvent.start.equals(eventToRemove.start))
            {
              if (checkIfEvent.eventOwner.equals(eventToRemove.eventOwner))
              {
                guestCalendar.GetListsOfEvents().remove(j);
                guestCalendar.wasAlerted.remove(j);
              }
            }
          }
        }
        //remove event from owners list of events
        userCalendar.GetListsOfEvents().remove(event);
        userCalendar.wasAlerted.remove(event);
      }
    }
    //if it is not group event and user is the owner remove the event
    else
    {
      if (eventToRemove.eventOwner.equals(user))
      {
        userCalendar.GetListsOfEvents().remove(event);
        userCalendar.wasAlerted.remove(event);
      }
    }
    return eventToRemove.eventName;
  }
  
  boolean InsertEvent(int position, Event toAdd)
  {
    //if there are no events scheduled just add new event
    if (calendarEvents.size() == 0)
    {
      calendarEvents.add(toAdd);
      wasAlerted.add(false);
      return true;
    }
    Event previous = calendarEvents.get(position);
    if (previous.eventType.equalsIgnoreCase("Open"))
    {
      //create two new open events to replace the old one
      Event newStart = new Event(previous.eventName, previous.eventOwner, previous.eventGuests, previous.start, toAdd.start
                                   ,previous.eventType);
      Event newEnd = new Event(previous.eventName, previous.eventOwner, previous.eventGuests, toAdd.end, previous.end
                                 ,previous.eventType);
      //add new start to the old open's position
      calendarEvents.add(position, newStart);
      wasAlerted.add(position, false);
      //add the new event to position +1
      calendarEvents.add(position+1, toAdd);
      wasAlerted.add(position+1, false);
      //and new end to position +2
      calendarEvents.add(position+2, newEnd);
      wasAlerted.add(position+2, false);
      //remove the old open event
      calendarEvents.remove(position+3);
      wasAlerted.remove(position+3);
    }
    else
    {
      calendarEvents.add(toAdd);
      wasAlerted.add(false);
    }
    return true;
  }
  
  int CheckEventAvailability(Event toCheck)
  {
    //for all events check if they are open
    for (int i = 0; i < calendarEvents.size(); i ++)
    {
      Event current = calendarEvents.get(i);
        if (current.eventType.equalsIgnoreCase("Open"))
        {
          //check that the open event starts before toCheck starts
          if (toCheck.start.after(current.start))
          {
            //check that the open event starts before tocheck's end adn open event ends after toCheck
            if (toCheck.end.after(current.start) && toCheck.end.before(current.end))
            {
              return i;
            }
          }
        }
        else
        {
          //it is not an open event, if current and toCheck overlap return -1
          if (toCheck.start.before(current.end) && toCheck.end.after(current.end))
          {
            return -1;
          }
          if (toCheck.end.before(current.end) && toCheck.end.after(current.start))
          {
            return -1;
          }
        }
      }
    return 0;
  }
  
  public synchronized String AddEvent(String eventName, String eventOwner, String[] eventGuests,
                         Date start, Date end, String eventType) throws RemoteException
  {
    //create new event and check if owner has that time available
    Event newEvent = new Event(eventName, eventOwner, eventGuests, start, end, eventType);
    int pos = CheckEventAvailability(newEvent);
    //add event at pos if there is a time available
    if (pos >= 0)
    {
      if (InsertEvent(pos, newEvent))
      {
        return "Event was added";
      }
    }
    return "Event could not be added";
  }
  
  public synchronized String[] RetrieveUsersEventsWithinRange(String user, Date _start, Date _end) throws RemoteException
  {
    ArrayList<String> eventsToReturn = new ArrayList<String>();
    int userIndex = CalendarManager.users.indexOf(user);
    //check that user could be found
    if (userIndex < 0)
    {
      System.out.println("user not found");
      return new String[0];
    }
    CalendarObject userCalendar = CalendarManager.calendarObjects.get(userIndex);
    for (int i = 0; i < userCalendar.calendarEvents.size(); i++)
    {
      Event current = userCalendar.calendarEvents.get(i);
      //user is owner of events show all events
      if (current.eventOwner.equals(user))
      {
        String eventString = "";
        //check that event starts within range of dates
        if (current.start.after(_start) && current.end.before(_end))
        {
          //add details of events to the string
          eventString = current.eventName + " " + current.eventOwner + " " + current.start.toString() + " "
            + current.end.toString() + " " + current.eventType;
          for (int j = 0; j < current.eventGuests.length; j++)
          {
            eventString += current.eventGuests[j];
          }
          eventsToReturn.add(eventString);
        }
      }
      //user is not owner show non private/group events
      else
      {
        if (!current.eventType.equalsIgnoreCase("Private") && !current.eventType.equalsIgnoreCase("Group"))
        {
          String eventString = "";
          //check that event starts and ends between dates given
          if (current.start.after(_start) && current.end.before(_end))
          {
            //add details of event to string
            eventString = current.eventName + " " + current.eventOwner + " " + current.start.toString() + " "
              + current.end.toString() + " " + current.eventType;
            for (int j = 0; j < current.eventGuests.length; j++)
            {
              eventString += current.eventGuests[j];
            }
            eventsToReturn.add(eventString);
          }
        }
      }
    }
    return eventsToReturn.toArray(new String[eventsToReturn.size()]);
  }
}