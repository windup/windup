//$Id: HotelBookingAction.java 5579 2007-06-27 00:06:49Z gavin $
package org.jboss.seam.example.booking;

import static javax.persistence.PersistenceContextType.EXTENDED;

import java.util.Calendar;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

@Stateful
@Name("hotelBooking")
@Restrict("#{identity.loggedIn}")
public class HotelBookingAction implements HotelBooking
{
   
   @PersistenceContext(type=EXTENDED)
   private EntityManager em;
   
   @In 
   private User user;
   
   @In(required=false) @Out
   private Hotel hotel;
   
   @In(required=false) 
   @Out(required=false)
   private Booking booking;
     
   @In
   private FacesMessages facesMessages;
      
   @In
   private Events events;
   
   @Logger 
   private Log log;
   
   private boolean bookingValid;
   
   @Begin
   public void selectHotel(Hotel selectedHotel)
   {
      hotel = em.merge(selectedHotel);
   }
   
   public void bookHotel()
   {      
      booking = new Booking(hotel, user);
      Calendar calendar = Calendar.getInstance();
      booking.setCheckinDate( calendar.getTime() );
      calendar.add(Calendar.DAY_OF_MONTH, 1);
      booking.setCheckoutDate( calendar.getTime() );
   }
   
   public void setBookingDetails()
   {
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.DAY_OF_MONTH, -1);
      if ( booking.getCheckinDate().before( calendar.getTime() ) )
      {
         facesMessages.addToControl("checkinDate", "Check in date must be a future date");
         bookingValid=false;
      }
      else if ( !booking.getCheckinDate().before( booking.getCheckoutDate() ) )
      {
         facesMessages.addToControl("checkoutDate", "Check out date must be later than check in date");
         bookingValid=false;
      }
      else
      {
         bookingValid=true;
      }
   }
   
   public boolean isBookingValid()
   {
      return bookingValid;
   }
   
   @End
   public void confirm()
   {
      em.persist(booking);
      facesMessages.add("Thank you, #{user.name}, your confimation number for #{hotel.name} is #{booking.id}");
      log.info("New booking: #{booking.id} for #{user.username}");
      events.raiseTransactionSuccessEvent("bookingConfirmed");
   }
   
   @End
   public void cancel() {}
   
   @Remove
   public void destroy() {}
}