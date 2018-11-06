package com.baeldung.api;

import org.springframework.stereotype.Component;

@Component
public class HotelBookingServiceImpl implements HotelBookingService {
    Booking bookRide(String pickUpLocation) throws BookingException {
        return new Booking();
    };
}
