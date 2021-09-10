package com.fishedee.batch_call.sample.basic;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParkingDTO {

    @Data
    public static class Floor{
        private Integer driverId;

        private List<Car> carList;

        public void setCarList(List<Car> carList){
            this.carList = carList;
        }

        public void setCarList2(Car carList){
        }
    }

    private List<Floor> floorList = new ArrayList<>();
}
