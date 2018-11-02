package com.baeldung.api;

import org.springframework.stereotype.Component;

public interface HotelBookingService  {
    Booking bookRide(String pickUpLocation) throws BookingException;
}
