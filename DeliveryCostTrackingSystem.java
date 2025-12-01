import java.util.*;

class Driver {
    String driverId;
    String name;

    public Driver(String driverId, String name) {
        this.driverId = driverId;
        this.name = name;
    }

    public String getDriverId() {
        return driverId;
    }

    public String getName() {
        return name;
    }
}

class Delivery {
    String deliveryId;
    String driverId;
    Integer startTime;
    Integer endTime;
    Integer cost;
    Boolean paid;

    public Delivery(String deliveryId, String driverId, Integer startTime, Integer endTime, Integer cost) {
        this.deliveryId = deliveryId;
        this.driverId = driverId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.cost = cost;
        this.paid = false;
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    public Integer getStartTime() {
        return startTime;
    }

    public Integer getEndTime() {
        return endTime;
    }

    public String getDriverId() {
        return driverId;
    }

    public Integer getCost() {
        return cost;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public Boolean getPaid() {
        return paid;
    }
}

public class DeliveryCostTrackingSystem {
    Map<String, Driver> drivers;
    private List<Delivery> deliveries;

    private Integer totalCost;
    private Integer unpaidCost;

    public DeliveryCostTrackingSystem() {
        this.drivers = new HashMap<>();
        this.deliveries = new ArrayList<>();
        this.totalCost = 0;
        this.unpaidCost = 0;
    }

    public void addDriver(String driverId) {
        Driver driver = new Driver(driverId, "Driver" + new Random());
        drivers.putIfAbsent(driverId, driver);
    }

    public void addDelivery(String driverId, int startTime, int endTime) {
        if (!drivers.containsKey(driverId)) {
            throw new RuntimeException("Driver does not exist.");
        }
        Delivery delivery = new Delivery(UUID.randomUUID().toString(), driverId, startTime, endTime, endTime - startTime);
        deliveries.add(delivery);
        int cost = delivery.getCost();
        totalCost += cost;
        unpaidCost += cost;
    }

    public Integer getTotalCost() {
        return totalCost;
    }

    public void payUpToTime(Integer upToTime) {
        for (Delivery delivery : deliveries) {
            if (delivery.getEndTime() <= upToTime && !delivery.getPaid()) {
                delivery.setPaid(true);
                unpaidCost -= delivery.getCost();
            }
        }
    }

    public Integer getCostToBePaid() {
        return unpaidCost;
    }

    public Integer getMaxActiveDriversInLast24Hours(Integer currentTime) {
        int windowStart = currentTime - 1440;
        // This is a straightforward O(number_of_deliveries) scan.
        Set<String> activeDrivers = new HashSet<>();
        for (Delivery delivery : deliveries) {
            // Quick skip if too old or too far in the future
            if (delivery.getEndTime() < windowStart) {
                continue; // ended before the window
            }
            if (delivery.getStartTime() > currentTime) {
                continue; // starts after the window
            }
            // Overlaps
            activeDrivers.add(delivery.getDriverId());
        }
        return activeDrivers.size();
    }
}

class DeliveryCostTrackingSystemDemo {
    public static void main(String[] args) {
        DeliveryCostTrackingSystem o = new DeliveryCostTrackingSystem();

        o.addDriver("D1");
        o.addDriver("D2");

        o.addDelivery("D1", 0, 10);
        o.addDelivery("D1", 20, 40);
        o.addDelivery("D2", 30, 50);

        System.out.println(o.getTotalCost());

        o.payUpToTime(45);

        System.out.println(o.getCostToBePaid());

        System.out.println(o.getMaxActiveDriversInLast24Hours(50));
    }
}
