package client;
import java.rmi.*;
import server.*;
public class AlertThread implements Runnable
{
  EventInterface threadCalendar;
  public AlertThread(EventInterface calendarObject)
  {
    threadCalendar = calendarObject;
  }
  //infinitely loop. if alertString is not empty print the alert
  public void run()
  {
    System.out.println("started");
    String alertString = "";
    while(true)
    {
      try
      {
        alertString = threadCalendar.EventAlert();
        if (alertString.equals(""))
        {
          
        }
        else
        {
          System.out.println(alertString);
        }
      }
      catch(Exception e)
      {
        System.out.println(e);
      }
    }
  }
}