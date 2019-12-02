package io.jmqtt;

import lombok.Getter;
import lombok.Setter;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 * 启动参数解析
 * Created by wangkun23 on 2019/10/26.
 */
public class LaunchOptions {

    @Setter
    @Getter
    @Argument(index = 0, usage = "options", metaVar = "<option>")
    private String option;

    @Setter
    @Getter
    @Option(name = "-h", aliases = {"--help"}, usage = "prints this message")
    private boolean helpNeeded;


    public boolean isHelpNeeded() {
        return helpNeeded;
    }

}
