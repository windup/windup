package com.baeldung.api;

public class SpringBean implements CabBookingService {
    Booking bookRide(String pickUpLocation) throws BookingException {
        return new Booking();
    }

}