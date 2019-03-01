package registerAllocation;

import java.util.Arrays;

public class RegisterAllocator
{
    private static boolean[] registers;
    private static final Integer REGISTER_CAPACITY = 32;

    private static RegisterAllocator registerAllocator;

    private RegisterAllocator()
    {
        Arrays.fill(registers, false);
        registers[0] = true;
        registers[31] = true;
    }

    public static RegisterAllocator getInstance()
    {
        if(registerAllocator == null)
        {
            registerAllocator = new RegisterAllocator();
        }

        return registerAllocator;
    }

    public Integer allocate()
    {
        for(int i = 0; i < REGISTER_CAPACITY; i++)
        {
            if(registers[i] == false)
            {
                registers[i] = true;
                return i;
            }
        }

        return -1;
    }

    public void deallocate(Integer no)
    {
        registers[no] = false;
    }
}