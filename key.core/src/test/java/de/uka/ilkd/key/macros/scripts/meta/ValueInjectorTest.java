package de.uka.ilkd.key.macros.scripts.meta;

import de.uka.ilkd.key.macros.scripts.LetCommand;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Alexander Weigl
 * @version 1 (02.05.17)
 */
public class ValueInjectorTest {
    @Test
    public void testInjectionSimple() throws Exception {
        PP pp = new PP();
        Map<String, String> args = new HashMap<>();
        args.put("b", "true");
        args.put("i", "42");
        args.put("s", "blubb");

        ValueInjector.injection(null, pp, args);

        assertEquals(true, pp.b);
        assertEquals(42, pp.i);
        assertEquals("blubb", pp.s);

    }

    @Test
    public void testRequired() throws Exception {
        PP pp = new PP();
        Map<String, String> args = new HashMap<>();
        args.put("b", "true");
        args.put("s", "blubb");
        assertThrows(ArgumentRequiredException.class,
            () -> ValueInjector.injection(new LetCommand(), pp, args));
    }

    @Test
    public void testInferScriptArguments() throws NoSuchFieldException {
        List<ProofScriptArgument> meta = ArgumentsLifter.inferScriptArguments(PP.class, null);
        assertEquals(3, meta.size());

        {
            ProofScriptArgument b = meta.get(0);
            assertEquals("b", b.getName());
            assertEquals(PP.class.getDeclaredField("b"), b.getField());
            assertEquals(Boolean.TYPE, b.getType());
            assertEquals(true, b.isRequired());
        }

        {
            ProofScriptArgument i = meta.get(1);
            assertEquals("i", i.getName());
            assertEquals(PP.class.getDeclaredField("i"), i.getField());
            assertEquals(Integer.TYPE, i.getType());
            assertEquals(true, i.isRequired());
        }

        {
            ProofScriptArgument i = meta.get(2);
            assertEquals("s", i.getName());
            assertEquals(PP.class.getDeclaredField("s"), i.getField());
            assertEquals(String.class, i.getType());
            assertEquals(false, i.isRequired());
        }

    }

    public static class PP {
        @Option("b")
        boolean b;
        @Option(value = "i", required = true)
        int i;
        @Option(value = "s", required = false)
        String s;
    }
}
