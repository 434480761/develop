package nd.esp.service.lifecycle.support;

/**
 * 简单的处理池
 * </p>
 *
 * @author bifeng.liu
 */
public class Pool {

    /**
     * 初始化池大小
     */
    private final int initialPoolSize;
    /**
     * 最大池大小
     */
    private final int maxPoolSize;
    /**
     * 生成处理对象工厂类
     */
    private final Factory factory;
    /**
     * 用于段中处理对象的池
     */
    private transient Object[] pool;
    /**
     * 保存下一个有效的索引
     */
    private transient int nextAvailable;
    /**
     * 用于处理锁
     */
    private transient Object mutex = new Object();

    public Pool(int initialPoolSize, int maxPoolSize, Factory factory) {
        this.initialPoolSize = initialPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.factory = factory;
    }

    /**
     * 从池中取得处理对象
     * <p/>
     * 如果池中没有处理对象，则会等待，直到调用putInPool，
     * 在对象调用了该方法后，在最后一定要调用putInPool方法把处理对象退出到池中。
     *
     * @return
     */
    public Object fetchFromPool() {
        Object result;
        synchronized (mutex) {
            if (pool == null) {
                pool = new Object[maxPoolSize];
                for (nextAvailable = initialPoolSize; nextAvailable > 0; ) {
                    putInPool(factory.newInstance());
                }
            }
            while (nextAvailable == maxPoolSize) {
                try {
                    mutex.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interrupted whilst waiting for a free item in the pool : " + e.getMessage());
                }
            }
            result = pool[nextAvailable++];
            if (result == null) {
                result = factory.newInstance();
                putInPool(result);
                ++nextAvailable;
            }
        }
        return result;
    }

    /**
     * 把对象设置到池中
     *
     * @param object
     */
    public void putInPool(Object object) {
        synchronized (mutex) {
            pool[--nextAvailable] = object;
            mutex.notify();
        }
    }

    private Object readResolve() {
        mutex = new Object();
        return this;
    }

    /**
     * 处理对象工厂接口,用于生成在池中使用的对象
     */
    public interface Factory {
        public Object newInstance();
    }
}