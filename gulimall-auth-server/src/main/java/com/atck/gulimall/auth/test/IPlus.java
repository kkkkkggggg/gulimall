package com.atck.gulimall.auth.test;

public class IPlus
{
    public int  plus(int i)
    {
        i = i++;
        return i;
    }

    public static void main(String[] args)
    {
        IPlus iPlus = new IPlus();
        int plus = iPlus.plus(1);
        System.out.println(plus);
    }
}
