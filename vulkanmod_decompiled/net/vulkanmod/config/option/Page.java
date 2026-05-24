/*
 * Decompiled with CFR 0.152.
 */
package net.vulkanmod.config.option;

import java.util.ArrayList;
import java.util.List;
import net.vulkanmod.config.gui.OptionBlock;
import net.vulkanmod.config.option.Option;

public class Page {
    private final String name;
    private final List<Block> blocks = new ArrayList<Block>();

    private Page(String name) {
        this.name = name;
    }

    public static Page of(String name) {
        return new Page(name);
    }

    public Block block(String title) {
        Block block = new Block(title, this);
        this.blocks.add(block);
        return block;
    }

    public static class Block {
        private final String title;
        private final List<Option<?>> options = new ArrayList();
        private final Page parent;

        private Block(String title, Page parent) {
            this.title = title;
            this.parent = parent;
        }

        public Block add(Option<?> option) {
            this.options.add(option);
            return this;
        }

        public Page done() {
            return this.parent;
        }

        private OptionBlock build() {
            return new OptionBlock(this.title, this.options.toArray(new Option[0]));
        }
    }
}

