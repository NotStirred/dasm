// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
//    contributors may be used to endorse or promote products derived from
//    this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.

package io.github.notstirred.dasm.util;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;

/**
 * A class responsible for remapping types and names.
 * <p>
 * <h2>This class differes from where it's copied as it also remaps primitives if specified.</h2>
 *
 * @author Eugene Kuleshov
 */
public abstract class RemapperWithPrimitives extends Remapper {

    /**
     * Returns the given descriptor, remapped with {@link #map(String)}.
     *
     * @param descriptor a type descriptor.
     * @return the given descriptor, with its [array element type] internal name remapped with {@link
     * #map(String)} (if the descriptor corresponds to an array or object type, otherwise the
     * descriptor is returned as is). See {@link Type#getInternalName()}.
     */
    public String mapDesc(final String descriptor) {
        return mapType(Type.getType(descriptor)).getDescriptor();
    }

    /**
     * Returns the given {@link Type}, remapped with {@link #map(String)} or {@link
     * #mapMethodDesc(String)}.
     *
     * @param type a type, which can be a method type.
     * @return the given type, with its [array element type] internal name remapped with {@link
     * #map(String)} (if the type is an array or object type, otherwise the type is returned as
     * is) or, of the type is a method type, with its descriptor remapped with {@link
     * #mapMethodDesc(String)}. See {@link Type#getInternalName()}.
     */
    private Type mapType(final Type type) {
        switch (type.getSort()) {
            case Type.ARRAY:
                StringBuilder remappedDescriptor = new StringBuilder();
                for (int i = 0; i < type.getDimensions(); ++i) {
                    remappedDescriptor.append('[');
                }
                remappedDescriptor.append(mapType(type.getElementType()).getDescriptor());
                return Type.getType(remappedDescriptor.toString());
            case Type.OBJECT:
                String remappedInternalName = map(type.getInternalName());
                return remappedInternalName != null ? Type.getObjectType(remappedInternalName) : type;
            case Type.METHOD:
                return Type.getMethodType(mapMethodDesc(type.getDescriptor()));
            default:
                return Type.getType(map(type.getInternalName()));
        }
    }

}

