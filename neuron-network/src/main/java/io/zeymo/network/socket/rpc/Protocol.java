package io.zeymo.network.socket.rpc;

/**
 * Created By Zeymo at 14/11/4 13:30
 */
public interface Protocol {

    public final static int SIZE_OF_BYTE = 1;

    public final static int SIZE_OF_INT = 4;

    public final static int SIZE_OF_LONG = 8;

    public final static int HEART_BEAT = 1;

    public final static int RPC = 2;

    public final static int FILE = 3;

    public final static int HAND_SHAKE = 4;

    public final static String TEST_FILE_PREFIX_PATH = "src/main/resources/";

    public static enum FileSubOpCode{
        OP_SYN_READ_FILE((byte)1),
        OP_READ_FILE((byte)2),
        OP_SYN_WRITE_FILE((byte)3),
        OP_WRITE_FILE((byte)4);

        public byte opCode;

        private FileSubOpCode(byte opCode) {
            this.opCode = opCode;
        }
    }

}
