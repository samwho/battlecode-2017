package samwho.perf;

import samwho.*;

import battlecode.common.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Unused, not allowed.
 *
 * Was fun to write, though.
 */
public class ProfilerProxy  implements InvocationHandler {
  private RobotController rc;
  private final Object target;
  private BytecodeCounter bc;

  public static <T> T create(RobotController rc, T target, Class<? extends T> iface) {
    return (T) Proxy.newProxyInstance(
        iface.getClassLoader(),
        new Class<?>[] { iface },
        new ProfilerProxy(rc, target));
  }

  public ProfilerProxy(RobotController rc, Object target) {
    this.rc = rc;
    this.target = target;
    this.bc = new BytecodeCounter(rc);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    int overhead = bc.lap();
    Object result = method.invoke(target, args);
    int cost = bc.lap();

    Utils.debug_out(target.getClass().getSimpleName() + "." +
        method.getName() + ":" + cost);

    return result;
  }
}
