package com.atck.gulimall.auth.test;

public class Son extends Father
{
    private int i  = test();
    private static int j = method();

    static {
        System.out.print("(6)");
    }
    Son()
    {
        System.out.print("(7)");
    }
    {
        System.out.print("(8)");
    }
    public int test()
    {
        System.out.print("(9)");
        System.out.println(this);
        return 1;
    }
    public static int method()
    {
        System.out.print("(10)");
        return 1;
    }

    public static void main(String[] args)
    {
        Father son = new Son();
        System.out.println();
        Son son1 = new Son();
    }
}
