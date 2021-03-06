/*
 * Copyright 2007 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.beanlib.provider.replicator;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.jcip.annotations.ThreadSafe;
import net.sf.beanlib.spi.BeanTransformerSpi;
import net.sf.beanlib.spi.replicator.ImmutableReplicatorSpi;

/**
 * Immutable Object Replicator that supports replication across heterogeneous enums.
 * 
 * @author Joe D. Velopar
 */
public class HeteroImmutableReplicator extends ImmutableReplicator {
    public static final Factory factory = new Factory();

    /**
     * Factory for {@link HeteroImmutableReplicator}
     * 
     * @author Joe D. Velopar
     */
    @ThreadSafe
    private static class Factory implements ImmutableReplicatorSpi.Factory {
        private Factory() {}

        @Override
        public HeteroImmutableReplicator newImmutableReplicatable(BeanTransformerSpi beanTransformer) {
            return new HeteroImmutableReplicator();
        }
    }

    public static HeteroImmutableReplicator newImmutableReplicatable(BeanTransformerSpi beanTransformer) {
        return HeteroImmutableReplicator.factory.newImmutableReplicatable(beanTransformer);
    }

    protected HeteroImmutableReplicator() {}

    @Override
    public <V, T> T replicateImmutable(V immutableFrom, Class<T> toClass) {
        T result = super.replicateImmutable(immutableFrom, toClass);

        if (result != null)
            return result;
        // Both are enums but different enums
        if (toClass.isEnum() && immutableFrom instanceof Enum) { // to and from are of different enums.
            Enum<?> enumFrom = (Enum<?>) immutableFrom;
            Method values;
            // Try to return the respective enum constant via ordinal
            try {
                values = toClass.getMethod("values");
                return toClass.cast(Array.get(values.invoke(null), enumFrom.ordinal()));
            } catch (ArrayIndexOutOfBoundsException ignore) {
                // Enums are of different sizes. Drop thru to return null
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        }
        return null;
    }
}
