package app;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class SimulateBooking {
    public static void main(String[] args) {

        // Step 1: Create a User
        UserService userService = new UserService();
        userService.registerUser("Pranjal Sharma", "pranjal@example.com");
        User user = userService.getUserManager().getUser("pranjal@example.com");

        // Step 2: Setup the Movie and Theatres
        Movie movie = new Movie("Inception");

        // Creating seats
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            seats.add(new BasicSeat("B" + i));
        }
        for (int i = 1; i <= 5; i++) {
            seats.add(new PremiumSeat("P" + i));
        }

        // Creating Shows
        Theatre theatre = new Theatre(new Address(City.DELHI, "110001", "Connaught Place"));
        Show show = new Show(theatre, seats, movie, LocalTime.of(18, 0), LocalTime.of(21, 0), 300);
        theatre.addShow(LocalDate.now(), show);

        // Step 3: Add the Theatre to the CityTheatreCatalogManager
        CityTheatreCatalogManager cityTheatreCatalogManager = new CityTheatreCatalogManager();
        cityTheatreCatalogManager.addTheatre(City.DELHI, theatre);

        // Step 4: Book Tickets
        BookingService bookingService = new BookingService(new CashPaymentStrategy());
        List<Seat> seatsToBook = Arrays.asList(seats.get(0), seats.get(1)); // Booking 2 seats
        String bookingId = bookingService.book(user, show, seatsToBook);

        System.out.println("Booking ID: " + bookingId);
    }
}

class User {
    private String name;
    private String emailId; // emailId is being used as the unique id for user identification in our system

    public User(String name, String emailId) {
        this.name = name;
        this.emailId = emailId;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getEmailId() {
        return emailId;
    }
}

class UserManager {
    Map<String, User> userCatalog;

    public UserManager() {
        userCatalog = new HashMap<>();
    }

    public void register(String name, String emailId) {
        if (!userCatalog.containsKey(emailId)) {
            userCatalog.put(emailId, new User(name, emailId));
        } else {
            throw new IllegalArgumentException("User with this email ID already exists.");
        }
    }

    public User getUser(String emailId) {
        return userCatalog.get(emailId);
    }
}

class UserService {
    private UserManager userManager;

    public UserService() {
        userManager = new UserManager();
    }

    public void registerUser(String name, String emailId) {
        userManager.register(name, emailId);
    }

    public UserManager getUserManager() {
        return userManager;
    }
}

class MovieService {
    private CityTheatreCatalogManager cityTheatreCatalogManager;

    public MovieService() {
        cityTheatreCatalogManager = new CityTheatreCatalogManager();
    }

    public List<Show> getShows(City city, LocalDate date) {
        List<Show> showsAvailable = new ArrayList<>();
        List<Theatre> theatres = cityTheatreCatalogManager.getTheatres(city);

        for (Theatre theatre : theatres) {
            List<Show> shows = theatre.getShowsByDate(date);
            if (shows != null) {
                showsAvailable.addAll(shows);
            }
        }

        return showsAvailable;
    }
}

class BookingService {
    private PaymentStrategy paymentStrategy;
    private BookingManager bookingManager;

    public BookingService(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
        this.bookingManager = new BookingManager();
    }

    public String book(User user, Show show, List<Seat> seats) {
        int amount = 0;

        for (Seat seat : seats) {
            if (seat.getStatus() == SeatStatus.BOOKED) {
                throw new IllegalStateException("Seat " + seat.getId() + " is already booked.");
            }
            if (seat.getType() == SeatType.BASIC) {
                amount += show.getPrice();
            } else {
                amount += show.getPrice() + 100;
            }
        }

        paymentStrategy.pay(amount);

        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.BOOKED);
        }

        return bookingManager.createBooking(user, show, seats, amount);
    }
}

class BookingManager {
    private Map<String, Booking> bookingCatalog;

    public BookingManager() {
        bookingCatalog = new HashMap<>();
    }

    public String createBooking(User user, Show show, List<Seat> seats, int amount) {
        String id = UUID.randomUUID().toString();
        Booking booking = new Booking(id, user, show, seats, amount);
        bookingCatalog.put(id, booking);
        return id;
    }
}

class Booking {
    private String id;
    private User user;
    private Show show;
    private List<Seat> seats;
    private int amount;

    public Booking(String id, User user, Show show, List<Seat> seats, int amount) {
        this.id = id;
        this.user = user;
        this.show = show;
        this.seats = seats;
        this.amount = amount;
    }

    // Getters
    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Show getShow() {
        return show;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public int getAmount() {
        return amount;
    }
}

interface PaymentStrategy {
    void pay(int amount);
}

class CashPaymentStrategy implements PaymentStrategy {
    @Override
    public void pay(int amount) {
        System.out.println("Paid using Cash: " + amount);
    }
}

class BasicSeat extends Seat {
    public BasicSeat(String id) {
        super(id, SeatType.BASIC);
    }
}

class PremiumSeat extends Seat {
    public PremiumSeat(String id) {
        super(id, SeatType.PREMIUM);
    }
}

class Seat {
    private String id;
    private SeatType type;
    private SeatStatus status;

    public Seat(String id, SeatType type) {
        this.id = id;
        this.type = type;
        this.status = SeatStatus.FREE; // Default status
    }

    // Getters
    public String getId() {
        return id;
    }

    public SeatType getType() {
        return type;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }
}

enum SeatStatus {
    BOOKED, FREE
}

enum SeatType {
    BASIC, PREMIUM
}

enum City {
    DELHI, MUMBAI, NCR
}

class Address {
    private City city;
    private String pincode;
    private String street;

    public Address(City city, String pincode, String street) {
        this.city = city;
        this.pincode = pincode;
        this.street = street;
    }

    // Getters
    public City getCity() {
        return city;
    }

    public String getPincode() {
        return pincode;
    }

    public String getStreet() {
        return street;
    }
}

class Theatre {
    private Map<LocalDate, List<Show>> showsCatalog;
    private Address address;

    public Theatre(Address address) {
        this.address = address;
        this.showsCatalog = new HashMap<>();
    }

    public void addShow(LocalDate date, Show show) {
        showsCatalog.computeIfAbsent(date, k -> new ArrayList<>()).add(show);
    }

    public List<Show> getShowsByDate(LocalDate date) {
        return showsCatalog.get(date);
    }

    // Getters
    public Address getAddress() {
        return address;
    }
}

class Show {
    private Theatre theatre;
    private List<Seat> seats;
    private Movie movie;
    private LocalTime startTime;
    private LocalTime endTime;
    private int price;

    public Show(Theatre theatre, List<Seat> seats, Movie movie, LocalTime startTime, LocalTime endTime, int price) {
        this.theatre = theatre;
        this.seats = seats;
        this.movie = movie;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
    }

    // Getters
    public Theatre getTheatre() {
        return theatre;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public Movie getMovie() {
        return movie;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int getPrice() {
        return price;
    }
}

class Movie {
    private String name;

    public Movie(String name) {
        this.name = name;
    }

    // Getters
    public String getName() {
        return name;
    }
}

class CityTheatreCatalogManager {
    private Map<City, List<Theatre>> cityTheatreMap;

    public CityTheatreCatalogManager() {
        cityTheatreMap = new HashMap<>();
    }

    public void addTheatre(City city, Theatre theatre) {
        cityTheatreMap.computeIfAbsent(city, k -> new ArrayList<>()).add(theatre);
    }

    public List<Theatre> getTheatres(City city) {
        return cityTheatreMap.get(city);
    }
}
