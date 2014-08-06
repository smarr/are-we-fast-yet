package benchmarks;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@BenchmarkMode(Mode.Throughput)
@State(Scope.Thread)
public class DynamicProxy {

	public DynamicProxy() {
		calcObj = new Calculator(3);
		calcProxy = (ICalculator) Proxy.newProxyInstance(
				Calculator.class.getClassLoader(),
				new Class<?>[] {ICalculator.class}, new JustCallInvocationHandler(calcObj));
	}

	private int x = 5;
	private ICalculator calcObj;
	private ICalculator calcProxy;

	public interface ICalculator {
		int add(int b);
	}

	public static class Calculator implements ICalculator {
		private int a;

		public Calculator(final int a) {
			this.a = a;
		}

		@Override
		public int add(final int b) {
			return a + b;
		}
	}

	public static class JustCallInvocationHandler implements InvocationHandler {
		private final ICalculator calc;

		public JustCallInvocationHandler(final ICalculator calc) {
			this.calc = calc;
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) {
			try {
				method.setAccessible(true);
				return method.invoke(calc, args);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				e.printStackTrace();
			}
			return null;
		}
	}


	@GenerateMicroBenchmark
	public int directAdd() {
		return calcObj.add(x);
	}

	@GenerateMicroBenchmark
	public int proxiedAdd() {
		return calcProxy.add(x);
	}
}
