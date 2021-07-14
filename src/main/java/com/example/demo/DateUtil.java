package com.example.demo;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtil {
    public String getTime(Date endDate){
        Date startDate = new Date();
        long diffTime = (endDate.getTime() - startDate.getTime());
        long diffInSeconds = Math.abs(TimeUnit.MILLISECONDS.toSeconds(diffTime));
        long diffInMinutes = Math.abs(TimeUnit.MILLISECONDS.toMinutes(diffTime));
        long diffInHours =Math.abs(TimeUnit.MILLISECONDS.toHours(diffTime));
        long diffInDays = Math.abs(TimeUnit.MILLISECONDS.toDays(diffTime));

        while(diffInHours / 24 > 0){
            diffInDays++;
            diffInHours = diffInHours % 24;
        }

        while(diffInMinutes / 60 > 0){
            diffInHours++;
            diffInMinutes = diffInMinutes % 60;
        }

        while(diffInSeconds / 60 > 0){
            diffInMinutes++;
            diffInSeconds = diffInSeconds % 60;
        }

        if(diffInDays == 0){
            if(diffInHours == 0){
                if(diffInMinutes == 0){
                    return "" + (diffInSeconds) + " seconds";
                }else{
                    return "" + (diffInMinutes) + " minutes";
                }
            }else{
                return "" + (diffInHours) + " hrs " + (diffInMinutes) + " minutes";
            }
        }else{
            return "" + (diffInDays) + " days";
        }
    }
}
