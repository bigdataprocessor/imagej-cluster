import ij.IJ;
import ij.Prefs;
import net.imagej.legacy.IJ1Helper;
import net.imagej.legacy.LegacyService;

public class RunMultipleInstances
{
    public static void main(final String... args) throws Exception
    {
        /*
        LegacyService legacy = new LegacyService();
        final IJ1Helper helper = legacy.getIJ1Helper();
        boolean rmib = helper.isRMIEnabled();
        */

        int RUN_SOCKET_LISTENER = 1 << 22;
        int options = 549474304;
        int rmi = options & RUN_SOCKET_LISTENER;
        int a = 1;
    }

}
