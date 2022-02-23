package com.atck.common.constant;

public class WareConstant
{
    public enum PurchaseStatusEnum
    {
        CREATED(0,"新建"),ASSIGNED(1,"已分配"),RECEIVED(2,"已领取"),FINISHED(3,"已完成"),ERROR(4,"有异常");

        private int status;
        private String msg;

        PurchaseStatusEnum(int status,String msg)
        {
            this.status = status;
            this.msg = msg;
        }

        public int getStatus()
        {
            return status;
        }

        public String getMsg()
        {
            return msg;
        }
    }

    public enum PurchaseDetailStatusEnum
    {
        CREATED(0,"新建"),ASSIGNED(1,"已分配"),RECEIVED(2,"正在采购"),FINISHED(3,"已完成"),ERROR(4,"采购失败");

        private int status;
        private String msg;

        PurchaseDetailStatusEnum(int status,String msg)
        {
            this.status = status;
            this.msg = msg;
        }

        public int getStatus()
        {
            return status;
        }

        public String getMsg()
        {
            return msg;
        }
    }
}
