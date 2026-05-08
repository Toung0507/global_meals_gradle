package com.example.global_meals_gradle.res;

public class MemberOrderCountRes extends BasicRes {
    private MemberData data;

    public MemberOrderCountRes() {
        super();
    }

    public MemberOrderCountRes(int code, String message) {
        super(code, message);
    }

    // 成功時使用的建構子
    public MemberOrderCountRes(int code, String message, int memberId, String phone, int orderCount, int regionsId) {
        super(code, message);
        this.data = new MemberData(memberId, phone, orderCount, regionsId);
    }

    public MemberData getData() {
        return data;
    }

    public void setData(MemberData data) {
        this.data = data;
    }

    // 內部類別：定義 Data 的結構
    public static class MemberData {
        private int memberId;
        private String phone;
        private int orderCount;
        private int regionsId;

        public MemberData(int memberId, String phone, int orderCount, int regionsId) {
            this.memberId = memberId;
            this.phone = phone;
            this.orderCount = orderCount;
            this.regionsId = regionsId;
        }

        public int getMemberId() {
            return memberId;
        }

        public String getPhone() {
            return phone;
        }

        public int getOrderCount() {
            return orderCount;
        }

        public int getRegionsId() {
            return regionsId;
        }
    }
}