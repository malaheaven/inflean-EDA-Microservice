package com.inflearn.eda.domain.model;

import com.inflearn.eda.domain.model.vo.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalCard {
    private RentalCardNo rentalCardNo;
    private IDName member;
    private RentalStatus rentalStatus;
    private LateFee lateFee;
    private List<RentalItem> rentalItemList = new ArrayList<RentalItem>();
    private List<ReturnItem> returnItemList = new ArrayList<ReturnItem>();

    public static RentalCard sample() {
        RentalCard rentalCard = new RentalCard();
        rentalCard.setRentalCardNo(RentalCardNo.createRentalCardNo());
        rentalCard.setMember(IDName.sample());
        rentalCard.setRentalStatus(RentalStatus.RENT_AVAILABLE);
        rentalCard.setLateFee(LateFee.sample());
        return rentalCard;
    }

    private void addRentalItem(RentalItem rentalItem) {
        this.rentalItemList.add(rentalItem);
    }

    private void removeRentalItem(RentalItem rentalItem) {
        this.rentalItemList.remove(rentalItem);
    }

    private void addReturnItem(ReturnItem returnItem) {
        this.returnItemList.add(returnItem);
    }

    // 대여카드 생성
    public static RentalCard createRentalCard(IDName creator) {
        RentalCard rentalCard = new RentalCard();
        rentalCard.setRentalCardNo(RentalCardNo.createRentalCardNo());
        rentalCard.setMember(creator);
        rentalCard.setRentalStatus(RentalStatus.RENT_AVAILABLE);
        rentalCard.setLateFee(LateFee.createLateFee());
        return rentalCard;
    }

    // 대여처리
    public RentalCard rentItem(Item item) {
        checkRentalAvailable();
        this.addRentalItem(RentalItem.createRentalItem(item));
        return this;
    }

    private void checkRentalAvailable() {
        if (this.rentalStatus == RentalStatus.RENT_UNAVAILABLE) {
            throw new IllegalArgumentException("대여 불가 상태입니다.");
        }

        if (this.rentalItemList.size() > 5) {
            throw new IllegalArgumentException("이미 5권을 대여했습니다.");
        }
    }

    // 반납 처리
    public RentalCard returnItem(Item item, LocalDate returnDate) {
        RentalItem rentalItem = this.rentalItemList.stream().filter(i -> i.getItem().equals(item)).findFirst().get();
        calculateLateFee(rentalItem, returnDate);
        this.addReturnItem(ReturnItem.createReturnItem(rentalItem));
        this.removeRentalItem(rentalItem);
        return this;
    }

    private void calculateLateFee(RentalItem rentalItem, LocalDate returnDate) {
        if (returnDate.compareTo(rentalItem.getOverdueDate()) > 0) {
            long point = Period.between(returnDate, rentalItem.getOverdueDate()).getDays() * 10;
            this.lateFee.addPoint(point);
        }
    }

    // 연체 처리
    public RentalCard overdueItem(Item item) {
        RentalItem rentalItem = this.rentalItemList.stream().filter(i -> i.getItem().equals(item)).findFirst().get();
        rentalItem.setOverdued(true);
        this.rentalStatus = RentalStatus.RENT_UNAVAILABLE;

        // 연체 억지로 만들기: 실제로 필요 없는 코드
        rentalItem.setOverdueDate(LocalDate.now().minusDays(1));
        return this;
    }

    // 연체 해제
    public long makeAvailableRental(long point) throws Exception {
        if (this.rentalItemList.size() != 0) {
            throw new IllegalArgumentException("헤당 포인트로 연체를 해제할 수 없습니다.");
        }

        if (this.getLateFee().getPoint() != point) {
            throw new IllegalArgumentException("헤당 포인트로 연체를 해제할 수 없습니다.");
        }

        this.setLateFee(lateFee.removePoint(point));

        if (this.lateFee.getPoint() == 0) {
            this.rentalStatus = RentalStatus.RENT_AVAILABLE;
        }

        return this.getLateFee().getPoint();
    }

}
