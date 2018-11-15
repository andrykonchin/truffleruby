/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * Some of the code in this class is transliterated from C++ code in Rubinius.
 *
 * Copyright (c) 2007-2014, Evan Phoenix and contributors
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of Rubinius nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.truffleruby.core;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleContext;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyLanguage;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.basicobject.BasicObjectNodes.ReferenceEqualNode;
import org.truffleruby.core.cast.NameToJavaStringNode;
import org.truffleruby.core.fiber.FiberManager;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.kernel.KernelNodesFactory;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.backtrace.Backtrace;
import org.truffleruby.language.control.ExitException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.ThrowException;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.LookupMethodNode;
import org.truffleruby.language.methods.LookupMethodNodeGen;
import org.truffleruby.language.objects.MetaClassNode;
import org.truffleruby.language.objects.shared.SharedObjects;
import org.truffleruby.language.yield.YieldNode;
import org.truffleruby.platform.Signals;

import java.util.Map.Entry;

@CoreClass(value = "VM primitives")
public abstract class VMPrimitiveNodes {

    @Primitive(name = "vm_catch", needsSelf = false)
    public abstract static class CatchNode extends PrimitiveArrayArgumentsNode {

        @Child private YieldNode dispatchNode = new YieldNode();

        @Specialization
        public Object doCatch(VirtualFrame frame, Object tag, DynamicObject block,
                @Cached("create()") BranchProfile catchProfile,
                @Cached("createBinaryProfile()") ConditionProfile matchProfile,
                @Cached("create()") ReferenceEqualNode referenceEqualNode) {
            try {
                return dispatchNode.dispatch(block, tag);
            } catch (ThrowException e) {
                catchProfile.enter();
                if (matchProfile.profile(referenceEqualNode.executeReferenceEqual(e.getTag(), tag))) {
                    return e.getValue();
                } else {
                    throw e;
                }
            }
        }
    }

    @Primitive(name = "vm_gc_start", needsSelf = false)
    public static abstract class VMGCStartPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject vmGCStart() {
            System.gc();
            return nil();
        }

    }

    // The hard #exit!
    @Primitive(name = "vm_exit", needsSelf = false, lowerFixnum = 1)
    public static abstract class VMExitPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public Object vmExit(int status) {
            throw new ExitException(status);
        }

        @Fallback
        public Object vmExit(Object status) {
            return FAILURE;
        }

    }

    @Primitive(name = "vm_extended_modules", needsSelf = false)
    public static abstract class VMExtendedModulesNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public Object vmExtendedModules(Object object, DynamicObject block,
                @Cached("create()") MetaClassNode metaClassNode,
                @Cached("new()") YieldNode yieldNode,
                @Cached("createBinaryProfile()") ConditionProfile isSingletonProfile) {
            final DynamicObject metaClass = metaClassNode.executeMetaClass(object);

            if (isSingletonProfile.profile(Layouts.CLASS.getIsSingleton(metaClass))) {
                for (DynamicObject included : Layouts.MODULE.getFields(metaClass).prependedAndIncludedModules()) {
                    yieldNode.dispatch(block, included);
                }
            }

            return nil();
        }

    }

    @Primitive(name = "vm_method_is_basic", needsSelf = false)
    public static abstract class VMMethodIsBasicNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public boolean vmMethodIsBasic(VirtualFrame frame, DynamicObject method) {
            return Layouts.METHOD.getMethod(method).isBuiltIn();
        }

    }

    @Primitive(name = "vm_method_lookup", needsSelf = false)
    public static abstract class VMMethodLookupNode extends PrimitiveArrayArgumentsNode {

        @Child private NameToJavaStringNode nameToJavaStringNode;
        @Child private LookupMethodNode lookupMethodNode;

        public VMMethodLookupNode() {
            nameToJavaStringNode = NameToJavaStringNode.create();
            lookupMethodNode = LookupMethodNodeGen.create(true, false);
        }

        @Specialization
        public DynamicObject vmMethodLookup(VirtualFrame frame, Object self, Object name) {
            // TODO BJF Sep 14, 2016 Handle private
            final String normalizedName = nameToJavaStringNode.executeToJavaString(name);
            InternalMethod method = lookupMethodNode.executeLookupMethod(frame, self, normalizedName);
            if (method == null) {
                return nil();
            }
            return Layouts.METHOD.createMethod(coreLibrary().getMethodFactory(), self, method);
        }

    }

    @Primitive(name = "vm_object_respond_to", needsSelf = false)
    public static abstract class VMObjectRespondToPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private KernelNodes.RespondToNode respondToNode = KernelNodesFactory.RespondToNodeFactory.create(null, null, null);

        @Specialization
        public boolean vmObjectRespondTo(VirtualFrame frame, Object object, Object name, boolean includePrivate) {
            return respondToNode.executeDoesRespondTo(frame, object, name, includePrivate);
        }

    }


    @Primitive(name = "vm_object_singleton_class", needsSelf = false)
    public static abstract class VMObjectSingletonClassPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private KernelNodes.SingletonClassMethodNode singletonClassNode = KernelNodesFactory.SingletonClassMethodNodeFactory.create(null);

        @Specialization
        public Object vmObjectClass(Object object) {
            return singletonClassNode.singletonClass(object);
        }

    }

    @Primitive(name = "vm_raise_exception", needsSelf = false)
    public static abstract class VMRaiseExceptionPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isRubyException(exception)")
        public DynamicObject vmRaiseException(DynamicObject exception, boolean internal,
                @Cached("createBinaryProfile()") ConditionProfile reRaiseProfile) {
            final Backtrace backtrace = Layouts.EXCEPTION.getBacktrace(exception);
            if (reRaiseProfile.profile(backtrace != null && backtrace.getTruffleException() != null)) {
                // We need to rethrow the existing TruffleException, otherwise we would lose the
                // incremental backtrace accumulated in it.
                throw (RaiseException) backtrace.getTruffleException();
            } else {
                throw new RaiseException(getContext(), exception, internal);
            }
        }

    }

    @Primitive(name = "vm_set_module_name", needsSelf = false)
    public static abstract class VMSetModuleNamePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public Object vmSetModuleName(Object object) {
            throw new UnsupportedOperationException("vm_set_module_name");
        }

    }

    @Primitive(name = "vm_throw", needsSelf = false)
    public abstract static class ThrowNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public Object doThrow(Object tag, Object value) {
            throw new ThrowException(tag, value);
        }

    }

    @Primitive(name = "vm_time", needsSelf = false)
    public abstract static class TimeNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public long time() {
            return System.currentTimeMillis() / 1000;
        }

    }

    @Primitive(name = "vm_watch_signal", needsSelf = false)
    public static abstract class VMWatchSignalPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = { "isRubyString(signalName)", "isRubyString(action)" })
        public boolean restoreDefault(DynamicObject signalName, DynamicObject action) {
            final String actionString = StringOperations.getString(action);
            switch (actionString) {
                case "DEFAULT":
                    return restoreDefaultHandler(StringOperations.getString(signalName));
                case "SYSTEM_DEFAULT":
                    return restoreSystemHandler(StringOperations.getString(signalName));
                default:
                    throw new UnsupportedOperationException(actionString);
            }
        }

        @Specialization(guards = { "isRubyString(signalName)", "isNil(nil)" })
        public boolean ignoreSignal(DynamicObject signalName, Object nil) {
            return registerHandler(StringOperations.getString(signalName), null);
        }

        @Specialization(guards = { "isRubyString(signalNameString)", "isRubyProc(proc)" })
        public boolean watchSignalProc(DynamicObject signalNameString, DynamicObject proc) {
            if (getContext().getThreadManager().getCurrentThread() != getContext().getThreadManager().getRootThread()) {
                // The proc will be executed on the main thread
                SharedObjects.writeBarrier(getContext(), proc);
            }

            final RubyContext context = getContext();

            final String signalName = StringOperations.getString(signalNameString);
            return registerHandler(signalName, () -> {
                if (context.getOptions().SINGLE_THREADED) {
                    RubyLanguage.LOGGER.severe("signal " + signalName + " caught but can't create a thread to handle it so ignoring");
                    return;
                }

                final DynamicObject rootThread = context.getThreadManager().getRootThread();
                final FiberManager fiberManager = Layouts.THREAD.getFiberManager(rootThread);

                // Workaround: we need to register with Truffle (which means going multithreaded),
                // so that NFI can get its context to call pthread_kill() (GR-7405).
                final TruffleContext truffleContext = context.getEnv().getContext();
                final Object prev = truffleContext.enter();
                try {
                    context.getSafepointManager().pauseAllThreadsAndExecuteFromNonRubyThread(true, (rubyThread, currentNode) -> {
                        if (rubyThread == rootThread &&
                                fiberManager.getRubyFiberFromCurrentJavaThread() == fiberManager.getCurrentFiber()) {
                            ProcOperations.rootCall(proc);
                        }
                    });
                } finally {
                    truffleContext.leave(prev);
                }
            });
        }

        @TruffleBoundary
        private boolean restoreDefaultHandler(String signalName) {
            if (getContext().getOptions().EMBEDDED) {
                RubyLanguage.LOGGER.warning("restoring default handler for signal " + signalName + " in embedded mode may interfere with other embedded contexts or the host system");
            }

            try {
                return Signals.restoreDefaultHandler(signalName);
            } catch (IllegalArgumentException e) {
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this));
            }
        }

        @TruffleBoundary
        private boolean restoreSystemHandler(String signalName) {
            if (getContext().getOptions().EMBEDDED) {
                RubyLanguage.LOGGER.warning("restoring system handler for signal " + signalName + " in embedded mode may interfere with other embedded contexts or the host system");
            }

            try {
                Signals.restoreSystemHandler(signalName);
            } catch (IllegalArgumentException e) {
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this));
            }
            return true;
        }

        @TruffleBoundary
        private boolean registerHandler(String signalName, Runnable newHandler) {
            if (getContext().getOptions().EMBEDDED) {
                RubyLanguage.LOGGER.warning("trapping signal " + signalName + " in embedded mode may interfere with other embedded contexts or the host system");
            }

            try {
                Signals.registerHandler(newHandler, signalName);
            } catch (IllegalArgumentException e) {
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this));
            }
            return true;
        }

    }

    @Primitive(name = "vm_get_config_item", needsSelf = false)
    public abstract static class VMGetConfigItemPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyString(key)")
        public Object get(DynamicObject key) {
            final Object value = getContext().getNativeConfiguration().get(StringOperations.getString(key));

            if (value == null) {
                return nil();
            } else {
                return value;
            }
        }

    }

    @Primitive(name = "vm_get_config_section", needsSelf = false)
    public abstract static class VMGetConfigSectionPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();
        @Child private YieldNode yieldNode = new YieldNode();

        @TruffleBoundary
        @Specialization(guards = { "isRubyString(section)", "isRubyProc(block)" })
        public DynamicObject getSection(DynamicObject section, DynamicObject block) {
            for (Entry<String, Object> entry : getContext().getNativeConfiguration().getSection(StringOperations.getString(section))) {
                final DynamicObject key = makeStringNode.executeMake(entry.getKey(), UTF8Encoding.INSTANCE, CodeRange.CR_7BIT);
                yieldNode.dispatch(block, key, entry.getValue());
            }

            return nil();
        }

    }

    @Primitive(name = "vm_set_class", needsSelf = false)
    public abstract static class VMSetClassPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isRubyClass(newClass)")
        public DynamicObject setClass(DynamicObject object, DynamicObject newClass) {
            SharedObjects.propagate(getContext(), object, newClass);
            synchronized (object) {
                Layouts.BASIC_OBJECT.setLogicalClass(object, newClass);
                Layouts.BASIC_OBJECT.setMetaClass(object, newClass);
            }
            return object;
        }

    }

    @Primitive(name = "vm_dev_urandom_bytes", needsSelf = false, lowerFixnum = 1)
    public abstract static class VMDevUrandomBytes extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "count >= 0")
        public DynamicObject readRandomBytes(int count,
                @Cached("create()") StringNodes.MakeStringNode makeStringNode) {
            final byte[] bytes = getContext().getRandomSeedBytes(count);

            return makeStringNode.executeMake(bytes, ASCIIEncoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

        @Specialization(guards = "count < 0")
        public DynamicObject negativeCount(int count) {
            throw new RaiseException(getContext(),
                    getContext().getCoreExceptions().argumentError(
                            getContext().getCoreStrings().NEGATIVE_STRING_SIZE.getRope(), this));
        }

    }

}
