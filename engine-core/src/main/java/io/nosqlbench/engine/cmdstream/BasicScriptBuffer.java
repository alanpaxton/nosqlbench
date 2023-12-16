/*
 * Copyright (c) 2022-2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package io.nosqlbench.engine.cmdstream;

import io.nosqlbench.adapters.api.templating.StrInterpolator;
import io.nosqlbench.nb.api.nbio.Content;
import io.nosqlbench.nb.api.nbio.NBIO;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BasicScriptBuffer implements ScriptBuffer {

    private final static Logger logger = LogManager.getLogger(Cmd.class);
    private final List<Cmd> commands = new ArrayList<>();
    private final StringBuilder sb = new StringBuilder();
    private final Map<String, String> scriptParams = new HashMap<>();

//    public BasicScriptBuffer() {
//        this.createPath = null;
//    }

    public BasicScriptBuffer() {
    }

    @Override
    public List<Cmd> getCommands() {
        return this.commands;
    }

    public ScriptBuffer add(BasicScriptBuffer buf) {
        sb.append(buf.getParsedScript());
        return this;
    }

    public ScriptBuffer add(Cmd cmd) {
        commands.add(cmd);
        Map<String, String> params = cmd.getArgMap();

        switch (cmd.getCmdType()) {
            case script:
//                sb.append(Cmd.toJSONParams("params", cmd.getParams(), false));
//                sb.append(";\n");
                combineGlobalParams(scriptParams, cmd);
                String scriptData = loadScript(cmd);
                sb.append(scriptData);
                break;
            case fragment:
//                sb.append(Cmd.toJSONParams("params", cmd.getParams(), false));
//                sb.append(";\n");
                combineGlobalParams(scriptParams, cmd);
                sb.append(cmd.getArgValue("fragment"));
                if (cmd.getArgValue("fragment").endsWith(";")) {
                    sb.append("\n");
                }
                break;
            case java:
            case start: // start activity
            case run: // run activity
            case await: // await activity
//            case stop: // stop activity
            case forceStop: // force stopping activity
            case waitMillis:

                sb.append("controller.").append(cmd.asScriptText()).append("\n");
////                // Sanity check that this can parse before using it
////                sb.append("controller.").append(cmd.toString()).append("(")
////                    .append(Cmd.toJSONBlock(cmd.getParams(), false))
////                    .append(");\n");
////                break;
//                sb.append("controller.awaitActivity(\"").append(cmd.getArg("alias_name")).append("\");\n");
//                break;
//                sb.append("controller.stop(\"").append(cmd.getArg("alias_name")).append("\");\n");
//                break;
//                long millis_to_wait = Long.parseLong(cmd.getArg("ms"));
//                sb.append("controller.waitMillis(").append(millis_to_wait).append(");\n");
                break;
        }
        return this;

    }

    /**
     * Merge the params from the command into the global params map, but ensure that users know
     * if they are overwriting values, which could cause difficult to find bugs in their scripts.
     *
     * @param scriptParams The existing global params map
     * @param cmd          The command containing the new params to merge in
     */
    private void combineGlobalParams(Map<String, String> scriptParams, Cmd cmd) {
        for (String newkey : cmd.getArgs().keySet()) {
            String newvalue = cmd.getArgMap().get(newkey);

            if (scriptParams.containsKey(newkey)) {
                logger.warn("command '" + cmd.getCmdType() + "' overwrote param '" + newkey + " as " + newvalue);
            }
            scriptParams.put(newkey, newvalue);
        }
    }

    @Override
    public ScriptBuffer add(Cmd... cmds) {
        for (Cmd cmd : cmds) {
            add(cmd);
        }
        return this;
    }

    @Override
    public String getParsedScript() {
        String scripttext = sb.toString();
//        String appended = "//@ sourceURL="+tocreate.toString()+"\n\n" + scripttext;
        return scripttext;
    }

    @Override
    public Map<String, String> getCombinedParams() {
        return scriptParams;
    }

    public static String assemble(List<Cmd> cmds) {
        ScriptBuffer script = new BasicScriptBuffer();
        for (Cmd command : cmds) {
            script.add(command);
        }
        return script.getParsedScript();
    }

    public static String loadScript(Cmd cmd) {
        String scriptData;
        String path = cmd.getArgValue("path");

        logger.debug(() -> "Looking for " + path);

        Content<?> one = NBIO.all().searchPrefixes("scripts").pathname(path).extensionSet("js").one();
        scriptData = one.asString();

        StrInterpolator interpolator = new StrInterpolator(cmd.getArgMap());
        scriptData = interpolator.apply(scriptData);
        return scriptData;
    }


    public BasicScriptBuffer add(String script) {
        sb.append(script);
        return this;
    }
}
