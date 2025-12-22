package com.example.mobilebanking.api.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response từ Google Directions API
 */
public class GoogleDirectionsResponse {
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("routes")
    private List<Route> routes;
    
    @SerializedName("error_message")
    private String errorMessage;
    
    public String getStatus() {
        return status;
    }
    
    public List<Route> getRoutes() {
        return routes;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public boolean isSuccessful() {
        return "OK".equals(status);
    }
    
    /**
     * Route - Tuyến đường
     */
    public static class Route {
        @SerializedName("summary")
        private String summary;
        
        @SerializedName("legs")
        private List<Leg> legs;
        
        @SerializedName("overview_polyline")
        private OverviewPolyline overviewPolyline;
        
        public String getSummary() {
            return summary;
        }
        
        public List<Leg> getLegs() {
            return legs;
        }
        
        public OverviewPolyline getOverviewPolyline() {
            return overviewPolyline;
        }
        
        public double getDistanceKm() {
            if (legs != null && !legs.isEmpty()) {
                return legs.get(0).getDistance().getValue() / 1000.0;
            }
            return 0;
        }
        
        public int getDurationMinutes() {
            if (legs != null && !legs.isEmpty()) {
                return (int) Math.ceil(legs.get(0).getDuration().getValue() / 60.0);
            }
            return 0;
        }
    }
    
    /**
     * Leg - Đoạn đường giữa 2 waypoint
     */
    public static class Leg {
        @SerializedName("distance")
        private Distance distance;
        
        @SerializedName("duration")
        private Duration duration;
        
        @SerializedName("steps")
        private List<Step> steps;
        
        @SerializedName("start_address")
        private String startAddress;
        
        @SerializedName("end_address")
        private String endAddress;
        
        public Distance getDistance() {
            return distance;
        }
        
        public Duration getDuration() {
            return duration;
        }
        
        public List<Step> getSteps() {
            return steps;
        }
        
        public String getStartAddress() {
            return startAddress;
        }
        
        public String getEndAddress() {
            return endAddress;
        }
    }
    
    /**
     * Step - Bước đi cụ thể
     */
    public static class Step {
        @SerializedName("distance")
        private Distance distance;
        
        @SerializedName("duration")
        private Duration duration;
        
        @SerializedName("html_instructions")
        private String htmlInstructions;
        
        @SerializedName("travel_mode")
        private String travelMode;
        
        @SerializedName("maneuver")
        private String maneuver;
        
        public Distance getDistance() {
            return distance;
        }
        
        public Duration getDuration() {
            return duration;
        }
        
        public String getHtmlInstructions() {
            return htmlInstructions;
        }
        
        public String getTravelMode() {
            return travelMode;
        }
        
        public String getManeuver() {
            return maneuver;
        }
        
        // Chuyển HTML instructions thành plain text
        public String getPlainInstructions() {
            if (htmlInstructions == null) return "";
            return htmlInstructions
                    .replaceAll("<[^>]*>", "")
                    .replaceAll("&nbsp;", " ")
                    .trim();
        }
    }
    
    /**
     * Distance - Khoảng cách
     */
    public static class Distance {
        @SerializedName("text")
        private String text;
        
        @SerializedName("value")
        private int value; // Giá trị bằng mét
        
        public String getText() {
            return text;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    /**
     * Duration - Thời gian
     */
    public static class Duration {
        @SerializedName("text")
        private String text;
        
        @SerializedName("value")
        private int value; // Giá trị bằng giây
        
        public String getText() {
            return text;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    /**
     * OverviewPolyline - Encoded polyline của toàn bộ route
     */
    public static class OverviewPolyline {
        @SerializedName("points")
        private String points;
        
        public String getPoints() {
            return points;
        }
    }
}
