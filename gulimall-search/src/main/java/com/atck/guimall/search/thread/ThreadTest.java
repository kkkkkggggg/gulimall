package com.atck.guimall.search.thread;

import java.util.concurrent.*;

public class ThreadTest
{
    //当前系统中只有少量线程池，每个异步任务，提交给线程池去执行即可
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);


    public static void main(String[] args) throws ExecutionException, InterruptedException
    {
        System.out.println("main------start------");
        // CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() ->
        // {
        //     System.out.println("当前线程：" + Thread.currentThread().getId());
        //     int i = 10 / 2;
        //     System.out.println("运行结果：" + i);
        // }, executorService);

        // CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() ->
        // {
        //     System.out.println("当前线程：" + Thread.currentThread().getId());
        //     int i = 10 / 0;
        //     System.out.println("运行结果：" + i);
        //     return i;
        // }, executorService).whenComplete((res,ex)->{
        //     System.out.println("异步任务成功完成了。。。结果是：" + res + ";异常是：" + ex);
        // }).exceptionally(throwable -> {
        //     //可以感知异常，同时返回默认值
        //     return 10;
        // });

        /**
         * 方法执行完成后的处理
         */
        // CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() ->
        //         {
        //             System.out.println("当前线程：" + Thread.currentThread().getId());
        //             int i = 10 / 2;
        //             System.out.println("运行结果：" + i);
        //             return i;
        //         }, executorService).handle((res,ex)->{
        //             if (res!=null)
        //             {
        //                 return res*2;
        //             }
        //             if (ex!=null)
        //             {
        //                 return 0;
        //             }
        //             return 0;
        // });

        /**
         * 线程串行化
         * 1.thenRun：不能获取到上一步的执行结果
         * .thenRunAsync(() ->
         *         {
         *             System.out.println("任务2启动了");
         *         });
         * 2.thenAccept: 能接收上一步结果，但是无返回值
         * .thenAcceptAsync((res)->{
         *             System.out.println("任务二启动了。。。。，上一步的结果：" + res);
         *         },executorService);
         * 3.thenApplyAsync:    能接收上一步结果，有返回值
         * .thenApplyAsync((res) ->
         *         {
         *             System.out.println("任务二启动了。。。。，上一步的结果：" + res);
         *             return "hello" + res;
         *         }, executorService);
         */

        /**
         * 两个都完成
         */
        CompletableFuture<Object> future01 = CompletableFuture.supplyAsync(() ->
        {
            System.out.println("任务1线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("任务1运行结果：" + i);
            return i;
        }, executorService);

        CompletableFuture<Object> future02 = CompletableFuture.supplyAsync(() ->
        {
            System.out.println("任务2线程：" + Thread.currentThread().getId());
            try
            {
                Thread.sleep(3000);
                System.out.println("任务2运行结束");
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            return "Hello";
        }, executorService);

        // future01.runAfterBothAsync(future02,() -> {
        //     System.out.println("任务3开始");
        // },executorService);

        // future01.thenAcceptBothAsync(future02,(integer, s) -> {
        //     System.out.println("任务三开始。。。" + integer + ":" + s);
        // },executorService);

        // CompletableFuture<String> future = future01.thenCombineAsync(future02, (integer, s) ->
        // {
        //     System.out.println("任务三开始。。。" + integer + ":" + s);
        //     return integer + "" + s;
        // }, executorService);

        /**
         * 完成一个任务
         * runAfterEitherAsync:不感知结果，也无返回值
         * acceptEitherAsync：感知结果，无返回值
         * applyToEitherAsync：感知结果，有返回值
         */
        // future01.runAfterEitherAsync(future02,() -> {
        //     System.out.println("任务3开始。。。。。");
        // },executorService);

        // future01.acceptEitherAsync(future02,integer -> {
        //     System.out.println("任务3开始：" + integer);
        // },executorService);

        // CompletableFuture<String> future = future01.applyToEitherAsync(future02, integer ->
        // {
        //     System.out.println("任务3开始：" + integer);
        //     return integer + "hello";
        // }, executorService);


        /**
         * 多任务组合
         * 1.allOf:等待所有任务完成
         * 2.anyOf：只要有一个任务完成
         */
        // CompletableFuture<Void> allOf = CompletableFuture.allOf(future01, future02);
        // allOf.get();//等待所有结果

        CompletableFuture.anyOf(future01,future02);
        System.out.println("main------end------");
    }

    public void threadTest(String[] args) throws ExecutionException, InterruptedException
    {
        System.out.println("main------start------");
        /**
         * 1.继承Thread类，重写run方法
         *         new Thread01().start();
         * 2.实现Runnable接口，实现run方法
         *          new Thread(new Runnable01()).start();
         * 3.实现Callable接口，实现call方法,配合FutureTask，接收返回值
         *         FutureTask<Integer> task = new FutureTask<>(new Callable01());
         *         new Thread(task).start();
         *         //阻塞式等待整个线程执行完成，获取返回结果
         *         Integer integer = task.get();
         *   在以后的业务代码中，以上三种创建新线程的方式都不会用
         * 4.给线程池直接提交任务
         *          executorService.execute(new Runnable01());
         *          1.创建线程池
         *              1）、Executors
         *              2)、ThreadPoolExecutor
         *
         *
         *  区别：
         *      1、2不能得到返回值，3可以获得返回值
         *      1、2、3都不能控制资源
         *      4可以控制资源，保证服务稳定
         */
        /**
         * 七大参数
         * 1.corePoolSize：核心线程数，线程池创建好以后就准备就绪的线程数量，就等待来接收异步任务去执行【只要线程池不销毁就一直存在，除非设置了allowCoreThreadTimeOut】
         * 2.maximumPoolSize：最大线程数，控制资源
         * 3.keepAliveTime：如果正在运行的线程数量大于core数量，
         *                  释放空闲线程，只要线程空闲时间大于keepAliveTime
         * 4.TimeUnit unit：时间单位
         * 5.BlockingQueue<Runnable> workQueue;阻塞队列，如果任务有很多，就会将目前多的任务放到队列，只要有线程空闲了，就会去队列中取出新的任务执行
         * 6.ThreadFactory threadFactory：线程的创建工厂
         * 7.RejectedExecutionHandler handler：如果队列满了，按照指定的拒绝策略拒绝执行任务
         *
         * 工作顺序
         * 1）、线程池创建，准备好core数量的核心线程，准备接收任务
         * 1.1、core满了，就将再进来的任务放入阻塞队列中，空闲的核心线程就会自己去阻塞队列中获取任务执行
         * 1.2、阻塞队列满了，就直接开新线程执行，最大只能开到max指定的数量
         * 1.3、max满了就用RejectedExecutionHandler拒绝任务
         * 1.4、max都执行完成，有很多空闲，在指定时间keepAliveTime以后，释放max-core这些线程
         *
         * LinkedBlockingQueue:默认是Integer最大值，内存不够
         *
         * 一个线程池 core：7 max:20 queue:50 100并发进来怎么分配
         * 7个会立即得到执行，50个会进到队列，再开13个进行执行，剩下的30个使用拒绝策略
         * 如果不想抛弃CallerRunsPolicy:
         */
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5,200,10,TimeUnit.SECONDS,new LinkedBlockingQueue<>(100000),Executors.defaultThreadFactory(),new ThreadPoolExecutor.AbortPolicy());

        // Executors.newFixedThreadPool();固定大小，core=max，都不可回收
        // Executors.newCachedThreadPool(); core是0，所有都可以回收
        // Executors.newScheduledThreadPool();定时任务的线程池
        // Executors.newWorkStealingPool();
        System.out.println("main------end------");
    }

    public static class Thread01 extends Thread
    {
        @Override
        public void run()
        {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }

    public static class Runnable01 implements Runnable
    {

        @Override
        public void run()
        {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }

    public static class Callable01 implements Callable<Integer>
    {

        @Override
        public Integer call() throws Exception
        {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
            return i;
        }
    }
}
