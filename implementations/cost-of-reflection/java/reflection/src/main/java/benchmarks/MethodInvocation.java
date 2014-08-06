/*
 * Copyright (c) 2005, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package benchmarks;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@BenchmarkMode(Mode.Throughput)
@State(Scope.Thread)
public class MethodInvocation {

	private int x = 1;

	private static final Method getOneMethodStaticFinal = getMethodMirror();
	private final Method getOneMethodFinal = getMethodMirror();
	private Method getOneMethodMutable;


	private static final MethodHandle getOneMethodHandleStaticFinal = getMethodHandle();
	private        final MethodHandle getOneMethodHandleFinal = getMethodHandle();
	private              MethodHandle getOneMethodHandleMutable;


	private static final Method getMethodMirror() {
		try {
			Method m = MethodInvocation.class.getMethod("getOne");
			m.setAccessible(true);
			return m;
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static final MethodHandle getMethodHandle() {
		try {
			MethodType mt = MethodType.methodType(int.class);
			return MethodHandles.lookup().findVirtual(MethodInvocation.class, "getOne", mt);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Setup
	public void init() {
		getOneMethodMutable       = getMethodMirror();
		getOneMethodHandleMutable = getMethodHandle();
	}

	public int getOne() {
		return x;
	}


    @GenerateMicroBenchmark
    public int testDirectCall() {
    	return x + getOne();
    }

    // Reflection

    @GenerateMicroBenchmark
    public int testReflectiveCallFromMutableVar() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	return x + (int) (getOneMethodMutable.invoke(this));
    }

    @GenerateMicroBenchmark
    public int testReflectiveCallFromFinalVar() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	return x + (int) (getOneMethodFinal.invoke(this));
    }

    @GenerateMicroBenchmark
    public int testReflectiveCallFromStaticFinalVar() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	return x + (int) (getOneMethodStaticFinal.invoke(this));
    }

    // Method Handle

    @GenerateMicroBenchmark
    public int testHandleCallFromMutableVar() throws Throwable {
    	return x + (int) (getOneMethodHandleMutable.invokeExact(this));
    }

    @GenerateMicroBenchmark
    public int testHandleCallFromFinalVar() throws Throwable {
    	return x + (int) (getOneMethodHandleFinal.invokeExact(this));
    }

    @GenerateMicroBenchmark
    public int testHandleCallFromStaticFinalVar() throws Throwable {
    	return x + (int) (getOneMethodHandleStaticFinal.invokeExact(this));
    }
}
