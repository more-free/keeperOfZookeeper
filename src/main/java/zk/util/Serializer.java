package zk.util;

import org.apache.zookeeper.ZKUtil;

import java.io.*;

/**
 * Created by kexu1 on 9/11/2014.
 *
 * by default uses java's native library to serialize / deserialize.
 * Other serializers (ex. Kryo, protobuf, etc.) are easy to join with a facet created.
 */
public interface Serializer {
    public default byte [] serialize(Object obj) throws IOException {
        try(ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objStream = new ObjectOutputStream(byteStream)) {

            objStream.writeObject(obj);
            return byteStream.toByteArray();
        }
    }


    public default Object deserialize(byte [] data) throws IOException, ClassNotFoundException {
        try(ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
            ObjectInputStream objStream = new ObjectInputStream(byteStream)) {

            return objStream.readObject();
        }
    }

    public static Serializer defaultSerializer = new Serializer() { };
}
