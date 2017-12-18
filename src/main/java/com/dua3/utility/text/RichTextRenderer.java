package com.dua3.utility.text;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.CustomBlock;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.commonmark.node.Visitor;

import com.dua3.utility.Pair;
import com.dua3.utility.lang.LangUtil;

/**
 * Render MD to RichText.
 */
class RichTextRenderer {

    public static final float DEFAULT_FONT_SIZE = 12;

    static class LiteralCollectingVisitor extends AbstractVisitor {
        private StringBuilder sb = new StringBuilder();

        @Override
        public String toString() {
            return sb.toString();
        }

        @Override
        public void visit(Text node) {
            sb.append(node.getLiteral());
            super.visit(node);
        }
    }

    static class RTVisitor extends AbstractVisitor {

        RunStyle setRunAttr(Style s) {
            return new RunStyle(s);
        }

        private class RunStyle implements AutoCloseable {
            final Style style;

            RunStyle(Style style) {
                push(TextAttributes.STYLE_START_RUN, style);
                this.style = style;
            }

            @Override
            public void close() {
                push(TextAttributes.STYLE_END_RUN, style);
            }

            private void push(String key, Style attr) {
                @SuppressWarnings("unchecked")
                List<Style> current = (List<Style>) app.getOrSupply(key, LinkedList::new);
                current.add(attr);
            }
        }

        private final RichTextBuilder app;
        private boolean atStartOfLine = true;

        /**
         * Enumeration of supported List types.
         */
        protected enum ListType {
            /** Unordered (bulleted) list. */
            UNORDERED,
            /** Ordered (numbered) list. */
            ORDERED
        }

        /** Stack of the currently processed lists. */
        private final Deque<Pair<ListType,AtomicInteger>> listStack = new LinkedList<>();

        /**
         * Update the item count for this list.
         * @return pair with list type and the item number for the new item (1-based)
         */
        protected Pair<ListType,Integer> newListItem() {
            LangUtil.check(!listStack.isEmpty(), "item definition is not inside list");
            Pair<ListType, AtomicInteger> current = listStack.peekLast();
            ListType type = current.first;
            int nr = current.second.incrementAndGet();
            return Pair.of(type, nr);
        }

        /**
         * Starts a new list definition.
         * @param type the type of the list
         */
        protected void startList(ListType type) {
            listStack.add(Pair.of(type, new AtomicInteger()));
        }

        /**
         * Closes the current list definition
         */
        protected void endList() {
            LangUtil.check(!listStack.isEmpty(), "there is no list open");
            listStack.removeLast();
        }

        public RTVisitor(RichTextBuilder app) {
            this.app = app;
        }

        @Override
        public void visit(BlockQuote node) {
			Style attr = createStyle(MarkDownStyle.BLOCK_QUOTE);
            appendNewLineIfNeeded();
            applyStyleAndVisit(node, attr, super::visit);
            appendNewLineIfNeeded();
        }

        private <N extends Node> void applyStyleAndVisit(N node, Style attr, Consumer<N> visitor, String prefix, String suffix) {
            try (RunStyle runStyle = new RunStyle(attr)) {
                appendText(prefix);
                visitor.accept(node);
                appendText(suffix);
            }
        }

        private <N extends Node> void applyStyleAndVisit(N node, Style attr, Consumer<N> visitor) {
            applyStyleAndVisit(node, attr, visitor, "", "");
        }

		@SafeVarargs
		private static Style createStyle(MarkDownStyle mds, Pair<String, Object>... args) {
			return Style.create(mds.name(), MarkDownStyle.CLASS, args);
		}

        @Override
        public void visit(BulletList node) {
            startList(ListType.UNORDERED);
            Style attr = createStyle(MarkDownStyle.BULLET_LIST);
            appendNewLineIfNeeded();
            applyStyleAndVisit(node, attr, super::visit);
            endList();
        }

        @Override
        public void visit(Code node) {
            Style attr = createStyle(MarkDownStyle.CODE);
            applyStyleAndVisit(node, attr, super::visit, node.getLiteral(), "");
        }

        private void appendText(String literal) {
            app.append(literal);
            if (!literal.isEmpty()) {
                atStartOfLine = literal.charAt(literal.length()-1)=='\n';
            }
        }

        @Override
        public void visit(CustomBlock node) {
            Style attr = createStyle(MarkDownStyle.CUSTOM_BLOCK);
            appendNewLineIfNeeded();
            applyStyleAndVisit(node, attr, super::visit);
            appendNewLineIfNeeded();
        }

        @Override
        public void visit(CustomNode node) {
            Style attr = createStyle(MarkDownStyle.CUSTOM_NODE);
            applyStyleAndVisit(node, attr, super::visit);
        }

        @Override
        public void visit(Document node) {
            Style attr = createStyle(MarkDownStyle.DOCUMENT);
            applyStyleAndVisit(node, attr, super::visit);
        }

        @Override
        public void visit(Emphasis node) {
            Style attr = createStyle(MarkDownStyle.EMPHASIS);
            applyStyleAndVisit(node, attr, super::visit);
        }

        @Override
        public void visit(FencedCodeBlock node) {
            Style attr = createStyle(MarkDownStyle.FENCED_CODE_BLOCK);
            applyStyleAndVisit(node, attr, super::visit, node.getLiteral(), "");
            appendNewLineIfNeeded();
        }

        @Override
        public void visit(HardLineBreak node) {
            Style attr = createStyle(MarkDownStyle.HARD_LINE_BREAK);
            applyStyleAndVisit(node, attr, super::visit, "\n", "");
            appendNewLineIfNeeded();
        }

        @Override
        public void visit(Heading node) {
            String id = extractText(node, (v, n) -> v.visit(n)).toLowerCase(Locale.ROOT).trim();
            Style attr = createStyle(MarkDownStyle.HEADING,
                    Pair.of(MarkDownStyle.ATTR_HEADING_LEVEL, node.getLevel()),
                    Pair.of(MarkDownStyle.ATTR_ID, id));
            appendNewLineIfNeeded();
            applyStyleAndVisit(node, attr, super::visit);
            appendNewLineIfNeeded();
        }

        @Override
        public void visit(HtmlBlock node) {
            Style attr = createStyle(MarkDownStyle.HTML_BLOCK);
            applyStyleAndVisit(node, attr, super::visit, node.getLiteral(), "");
            appendNewLineIfNeeded();
        }

        @Override
        public void visit(HtmlInline node) {
            Style attr = createStyle(MarkDownStyle.HTML_INLINE);
            applyStyleAndVisit(node, attr, super::visit, node.getLiteral(), "");
        }

        @Override
        public void visit(Image node) {
            String altText = extractText(node, (v, n) -> v.visit(n));

            Style attr = createStyle(MarkDownStyle.IMAGE,
                    Pair.of(MarkDownStyle.ATTR_IMAGE_SRC, node.getDestination()),
                    Pair.of(MarkDownStyle.ATTR_IMAGE_TITLE, node.getTitle()),
                    Pair.of(MarkDownStyle.ATTR_IMAGE_ALT, altText));

            applyStyleAndVisit(node, attr, n -> {/*TODO*/});
        }

        @Override
        public void visit(IndentedCodeBlock node) {
            Style attr = createStyle(MarkDownStyle.INDENTED_CODE_BLOCK);
            applyStyleAndVisit(node, attr, super::visit, node.getLiteral(), "");
            appendNewLineIfNeeded();
        }

        @Override
        public void visit(Link node) {
            Style attr = createStyle(MarkDownStyle.LINK,
                    Pair.of(MarkDownStyle.ATTR_LINK_HREF, node.getDestination()),
                    Pair.of(MarkDownStyle.ATTR_LINK_TITLE, node.getTitle()),
                    Pair.of(MarkDownStyle.ATTR_LINK_EXTERN, !node.getDestination().startsWith("#")));
            applyStyleAndVisit(node, attr, super::visit);
        }

        @Override
        public void visit(ListItem node) {
            Pair<ListType,Integer> item = newListItem();
            Style attr = createStyle(MarkDownStyle.LIST_ITEM,
                    Pair.of(MarkDownStyle.ATTR_LIST_ITEM_TYPE, item.first),
                    Pair.of(MarkDownStyle.ATTR_LIST_ITEM_NR, item.second),
                    Pair.of(TextAttributes.TEXT_PREFIX, getListItemPrefix(item.first, item.second))
                );
            appendNewLineIfNeeded();
            applyStyleAndVisit(node, attr, super::visit);
            appendNewLineIfNeeded();
        }

        private String getListItemPrefix(ListType type, int itemNr) {
            switch (type) {
            case ORDERED:
                return " "+Integer.toString(itemNr)+". ";
            case UNORDERED:
                return " • ";
            default:
                throw new IllegalStateException();
            }
        }

        @Override
        public void visit(OrderedList node) {
            startList(ListType.ORDERED);
            Style attr = createStyle(MarkDownStyle.ORDERED_LIST);
            appendNewLineIfNeeded();
            applyStyleAndVisit(node, attr, super::visit);
            appendNewLineIfNeeded();
            endList();
        }

        @Override
        public void visit(Paragraph node) {
            Style attr = createStyle(MarkDownStyle.PARAGRAPH);
            applyStyleAndVisit(node, attr, super::visit);
            appendText("\n");
        }

        private void appendNewLineIfNeeded() {
            if (!atStartOfLine) {
                appendNewLine();
            }
        }

        private void appendNewLine() {
            app.append('\n');
            atStartOfLine = true;
        }

        @Override
        public void visit(SoftLineBreak node) {
            Style attr = createStyle(MarkDownStyle.SOFT_LINE_BREAK);
            applyStyleAndVisit(node, attr, super::visit, /* TODO: soft linebreak in RichText? */ "", "");
            appendNewLineIfNeeded();
        }

        @Override
        public void visit(StrongEmphasis node) {
            Style attr = createStyle(MarkDownStyle.STRONG_EMPHASIS);
            applyStyleAndVisit(node, attr, super::visit);
        }

        @Override
        public void visit(Text node) {
            appendText(node.getLiteral());
            super.visit(node);
        }

        @Override
        public void visit(ThematicBreak node) {
            Style attr = createStyle(MarkDownStyle.THEMATIC_BREAK);
            applyStyleAndVisit(node, attr, super::visit);
            appendNewLineIfNeeded();
        }

        private <N extends Node> String extractText(N node, BiConsumer<? super Visitor, N> consumer) {
            LiteralCollectingVisitor lcv = new LiteralCollectingVisitor();
            consumer.accept(lcv, node);
            return lcv.toString();
        }

    }

    public RichText render(Node node) {
        RichTextBuilder app = new RichTextBuilder();
        render(node, app);
        return app.toRichText();
    }

    public void render(Node node, RichTextBuilder app) {
        node.accept(new RTVisitor(app));
    }
}
