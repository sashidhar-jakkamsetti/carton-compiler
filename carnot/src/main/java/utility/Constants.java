package utility;

public class Constants
{
    public static final Integer SCANNER_IDENTIFIER_ADDRESS_OFFSET = 0;
    public static final Integer FILE_READER_END_OF_FILE_CHAR = 255;

    public static final Integer ARRAY_ADDRESS_OFFSET = 1000;

    public static final Integer INSTRUCTION_START_COUNTER = 0;
    public static final Integer BLOCK_START_COUNTER = 0;
    public static final Integer FORMAL_PARAMETER_VERSION = -1;
    public static final Integer GLOBAL_VARIABLE_VERSION = -2;
    public static final Integer NUMBER_OF_INSTRUCTIONS_CAP = 10000;

    public static final Integer CLUSTER_OFFSET = 10000;
    public static final Integer REGISTER_SIZE = 8;
    public static final Integer SPILL_REGISTER_OFFSET = 100;

    public static final Integer R0 = 0; // Zero register
    public static final Integer R_RETURN_ADDRESS = 31; // Return address Pointer
    public static final Integer R_GLOBAL_POINTER = 30; // Global Variable Address Pointer
    public static final Integer R_STACK_POINTER = 29; // Stack Pointer
    public static final Integer R_FRAME_POINTER = 28; // Frame Pointer
    public static final Integer R_TEMP = 27; // Temporary register
    public static final Integer R_PROXY_OFFSET = 26; // Proxy register for spilled regs

    public static final Integer BYTE_SIZE = 4;
}