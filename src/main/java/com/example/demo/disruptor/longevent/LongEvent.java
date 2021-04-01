package com.example.demo.disruptor.longevent;

import lombok.Getter;

/**
 * @author IMI-Ron
 */
@Getter
public class LongEvent {

    private Long value;

    /**
     * 通过Disruptor传递数据时，对象的生存期可能比预期的更长。
     * 为避免发生这种情况，可能需要在处理事件后清除事件。如果只有一个事件处理程序，则需要在处理器中清除对应的对象。
     * 如果您有一连串的事件处理程序，则可能需要在该链的末尾放置一个特定的处理程序来处理清除对象
     */
    void clear() {
        value = null;
    }

    public void set(long value) {
        this.value = value;
    }
}
