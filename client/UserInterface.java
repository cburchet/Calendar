package client;
import server.*;
import java.rmi.*;
import java.util.*;
import java.text.*;
public class UserInterface
{
  static CalendarRemoteInterface manager;
  static EventInterface usersCalendar;
  
  public static void main(String[] args)
  {
    try
    {
      System.setSecurityManager(new SecurityManager());
      Scanner userInput = new Scanner(System.in);
      manager = (CalendarRemoteInterface)Naming.lookup("rmi://localhost:65380/CalendarManager");
      String user = "";
      String calendarCreated = " ";
      //loop asking for name of user until the user gives a name that does not have a calendar already made
      do{
        System.out.println("Enter name of user to create");
        user = userInput.nextLine();
        calendarCreated = manager.MakeCalendar(user);
        System.out.println(calendarCreated);
      }while(!user.equals(calendarCreated));
      if (calendarCreated.equals(user))
      {
        String option = "";
        //get access to calendar and start the alert thread
        usersCalendar = manager.GetAccessToCalendar(user);
        Thread thread = new Thread(new AlertThread(usersCalendar));
        thread.setDaemon(true);
        thread.start();
        //todo: improve robustness by handling mistakes
        while(!option.equalsIgnoreCase("Exit"))
        {
          //ask user for what they want to do
          System.out.println("Make choice: Schedule, Delete, Calendars, Users, Retrieve, Exit");
          option = userInput.nextLine();
          //user wants to add event
          if (option.equalsIgnoreCase("Schedule"))
          {
            ArrayList<String> scheduledGuests = new ArrayList<String>();
            int guestSize = 0;
            //get event name
            System.out.println("Enter the event name");
            String scheduledName = userInput.nextLine();
            //get what type of event
            System.out.println("Enter the event type: public, private, group, open");
            String scheduledType = userInput.nextLine();
            Boolean eventValid = false;
            //user asked for group event
              if (scheduledType.equalsIgnoreCase("group"))
              {
                eventValid = true;
                String guests = "";
                //loop until done is input
                //Note: this allows for the addition of guests that do not exist, but the event will not be added
                //if the user's calendar does not exist 
                while(!guests.equalsIgnoreCase("Done"))
                {
                  System.out.println("Enter a guest name or Done to finish");
                  guests = userInput.nextLine();
                  //add the guests name
                  if (!guests.equalsIgnoreCase("Done"))
                  {
                    scheduledGuests.add(guests);
                    System.out.println(guests + " added");
                    guestSize++;
                  }
                  else
                  {
                    System.out.println(guests + " not added");
                  }
                }
              }
              //user asked for open, private, or public event
              else if (scheduledType.equalsIgnoreCase("open") || scheduledType.equalsIgnoreCase("public") 
                         || scheduledType.equalsIgnoreCase("private"))
              {
                eventValid = true;
              }
              //user did not input a valid event type
              else
              {
                eventValid = false;
                System.out.println("Invalid event type");
              }
              if (eventValid == true)
              {
                //ask for start and end date
                System.out.println("Enter the start date: MM/dd/yyyy/HH:mm");
                String startDate = userInput.nextLine();
                System.out.println("Enter the end date: MM/dd/yyyy/HH:mm");
                String endDate = userInput.nextLine();
                DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy/HH:mm");
                Date startFormatted = null;
                Date endFormatted = null;
                //ensure that the dates are valid
                try
                {
                  startFormatted = formatter.parse(startDate);
                  endFormatted = formatter.parse(endDate);
                }
                catch(Exception e)
                {
                  System.out.println(e);
                }
                if (startFormatted != null && endFormatted !=null)
                {
                  //add event for owner
                  String eventAdded = usersCalendar.AddEvent(scheduledName, user, scheduledGuests.toArray(new String[scheduledGuests.size()]), 
                                                             startFormatted, endFormatted, scheduledType);
                  //for each guest check that they exist in manager's user list and add the event for them
                  for (int j = 0; j < guestSize; j++)
                  {
                    String[] userList = manager.GetAllUsers();
                    for (int k = 0; k < userList.length; k++)
                    {
                      String guest = scheduledGuests.get(j);
                      if (userList[k].equals(guest))
                      {
                          EventInterface userCalendar = manager.GetAccessToCalendar(userList[k]);
                          userCalendar.AddEvent(scheduledName, user, scheduledGuests.toArray(new String[scheduledGuests.size()]), 
                                                startFormatted, endFormatted, scheduledType);
                      }
                    }
                  }
                  //show all of owners events after the addition
                  System.out.println(eventAdded);
                  String[] allEvents = usersCalendar.ReturnAllEventsForUser(user);
                  for (int i = 0; i < allEvents.length; i++)
                  {
                    System.out.println(allEvents[i]);
                  }
                }
                else
                {
                  System.out.println("Incorrect date formatting was entered");
                }
              }
          }
          else if (option.equalsIgnoreCase("Delete"))
          {
            //print the options for events to delete
            String[] allEvents = usersCalendar.ReturnAllEventsForUser(user);
            for (int i = 0; i < allEvents.length; i++)
            {
              System.out.println(Integer.toString(i) + allEvents[i]);
            }
            //allow the user to choose an event
            System.out.println("Choose an event");
            int event = userInput.nextInt();
            if (event > 0 && event < allEvents.length)
            {
              //remove the event and print the new list of events
              String eventRemoved = usersCalendar.RemoveEvent(user, event);
              System.out.println(eventRemoved);
              String[] allEventsAfter = usersCalendar.ReturnAllEventsForUser(user);
              for (int i = 0; i < allEventsAfter.length; i++)
              {
                System.out.println(Integer.toString(i) + allEventsAfter[i]);
              }
            }
            else
            {
              System.out.println(Integer.toString(event) + " does not exist");
            }
          }
          else if (option.equalsIgnoreCase("Calendars"))
          {
            //get name of user whose calendar should be show
            System.out.println("Enter name of user whose calendar you want to view");
            String userToView = userInput.nextLine();
            String[] allEvents = usersCalendar.ReturnAllEventsForUser(userToView);
            for (int i = 0; i < allEvents.length; i++)
            {
              System.out.println(allEvents[i]);
            }
          }
          else if (option.equalsIgnoreCase("Users"))
          {
            //print the names of all users
            String[] allUsers = manager.GetAllUsers();
            for (int i = 0; i < allUsers.length; i++)
            {
              System.out.println(allUsers[i]);
            }
          }
          else if (option.equalsIgnoreCase("Retrieve"))
          {
            //get name of user whose events you want to retrieve
            System.out.println("Enter the user whose events you want to retrieve");
            String userEventsToRetrieve = userInput.nextLine();
            //get start and end dates
            System.out.println("Enter the start date: MM/dd/yyyy/HH:mm");
            String startDate = userInput.nextLine();
            System.out.println("Enter the end date: MM/dd/yyyy/HH:mm");
            String endDate = userInput.nextLine();
            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy/HH:mm");
            //check that the dates are valid
            Date endFormatted = null;
            Date startFormatted = null;
            try
            {
              startFormatted = formatter.parse(startDate);
              endFormatted = formatter.parse(endDate);
            }
            catch(Exception e)
            {
              System.out.println(e);
              continue;
            }
            if (startFormatted != null && endFormatted != null)
            {
              String[] eventsInRange = usersCalendar.RetrieveUsersEventsWithinRange(userEventsToRetrieve, startFormatted, endFormatted);
              for (int i = 0; i < eventsInRange.length; i++)
              {
                System.out.println(eventsInRange[i]);
              }
            }
          }
        }
      }
      else
      {
        System.out.println(calendarCreated);
      }
    }
    catch(Exception e)
    {
      System.out.println(e);
    }
  }
}