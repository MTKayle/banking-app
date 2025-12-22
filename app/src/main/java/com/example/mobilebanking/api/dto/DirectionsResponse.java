package com.example.mobilebanking.api.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response từ TrackAsia Directions API
 */
public class DirectionsResponse {
    
    @SerializedName("code")
    private String code;
    
    @SerializedName("routes")
    private List<Route> routes;
    
    @SerializedName("waypoints")
    private List<Waypoint> waypoints;
    
    public String getCode() {
        return code;
    }
    
    public List<Route> getRoutes() {
        return routes;
    }
    
    public List<Waypoint> getWaypoints() {
        return waypoints;
    }
    
    public boolean isSuccessful() {
        return "Ok".equals(code);
    }
    
    /**
     * Route - Tuyến đường
     */
    public static class Route {
        @SerializedName("distance")
        private double distance; // Khoảng cách (mét)
        
        @SerializedName("duration")
        private double duration; // Thời gian (giây)
        
        @SerializedName("geometry")
        private String geometry; // Encoded polyline hoặc GeoJSON
        
        @SerializedName("legs")
        private List<Leg> legs;
        
        public double getDistance() {
            return distance;
        }
        
        public double getDuration() {
            return duration;
        }
        
        public String getGeometry() {
            return geometry;
        }
        
        public List<Leg> getLegs() {
            return legs;
        }
        
        public double getDistanceKm() {
            return distance / 1000.0;
        }
        
        public int getDurationMinutes() {
            return (int) Math.ceil(duration / 60.0);
        }
    }
    
    /**
     * Leg - Đoạn đường giữa 2 waypoint
     */
    public static class Leg {
        @SerializedName("distance")
        private double distance;
        
        @SerializedName("duration")
        private double duration;
        
        @SerializedName("steps")
        private List<Step> steps;
        
        @SerializedName("summary")
        private String summary;
        
        public double getDistance() {
            return distance;
        }
        
        public double getDuration() {
            return duration;
        }
        
        public List<Step> getSteps() {
            return steps;
        }
        
        public String getSummary() {
            return summary;
        }
    }
    
    /**
     * Step - Bước đi cụ thể (rẽ trái, rẽ phải, đi thẳng...)
     */
    public static class Step {
        @SerializedName("distance")
        private double distance;
        
        @SerializedName("duration")
        private double duration;
        
        @SerializedName("geometry")
        private String geometry;
        
        @SerializedName("name")
        private String name; // Tên đường
        
        @SerializedName("mode")
        private String mode;
        
        @SerializedName("maneuver")
        private Maneuver maneuver;
        
        public double getDistance() {
            return distance;
        }
        
        public double getDuration() {
            return duration;
        }
        
        public String getGeometry() {
            return geometry;
        }
        
        public String getName() {
            return name;
        }
        
        public String getMode() {
            return mode;
        }
        
        public Maneuver getManeuver() {
            return maneuver;
        }
    }
    
    /**
     * Maneuver - Hành động cụ thể (rẽ, quẹo...)
     */
    public static class Maneuver {
        @SerializedName("type")
        private String type; // turn, depart, arrive, etc.
        
        @SerializedName("modifier")
        private String modifier; // left, right, straight, etc.
        
        @SerializedName("instruction")
        private String instruction; // Hướng dẫn bằng văn bản
        
        @SerializedName("location")
        private List<Double> location; // [lng, lat]
        
        public String getType() {
            return type;
        }
        
        public String getModifier() {
            return modifier;
        }
        
        public String getInstruction() {
            return instruction;
        }
        
        public List<Double> getLocation() {
            return location;
        }
    }
    
    /**
     * Waypoint - Điểm trên đường đi
     */
    public static class Waypoint {
        @SerializedName("name")
        private String name;
        
        @SerializedName("location")
        private List<Double> location; // [lng, lat]
        
        public String getName() {
            return name;
        }
        
        public List<Double> getLocation() {
            return location;
        }
    }
}
