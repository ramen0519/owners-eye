import { useState, useRef, useEffect } from 'react';
import { sendChat } from '../api/insight';
import styles from './FloatingChat.module.css';

export default function FloatingChat() {
  const storeId = localStorage.getItem('storeId');
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([
    { role: 'ai', text: '안녕하세요! 매출에 관해 궁금한 것을 질문해주세요.' }
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const bottomRef = useRef(null);

  useEffect(() => {
    if (open) bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, open]);

  const handleSend = async (e) => {
    e.preventDefault();
    if (!input.trim() || loading) return;
    const question = input.trim();
    setInput('');
    setMessages((prev) => [...prev, { role: 'user', text: question }]);
    setLoading(true);
    try {
      const res = await sendChat(storeId, question);
      setMessages((prev) => [...prev, { role: 'ai', text: res.data.data.answer }]);
    } catch {
      setMessages((prev) => [...prev, { role: 'ai', text: '오류가 발생했습니다. 다시 시도해주세요.' }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      {open && (
        <div className={styles.panel}>
          <div className={styles.panelHeader}>
            <span className={styles.panelTitle}>AI 채팅</span>
            <button className={styles.closeBtn} onClick={() => setOpen(false)}>✕</button>
          </div>
          <div className={styles.chatArea}>
            {messages.map((msg, i) => (
              <div key={i} className={msg.role === 'user' ? styles.userMsg : styles.aiMsg}>
                {msg.role === 'ai' && <span className={styles.aiLabel}>AI</span>}
                <p className={styles.msgText}>{msg.text}</p>
              </div>
            ))}
            {loading && (
              <div className={styles.aiMsg}>
                <span className={styles.aiLabel}>AI</span>
                <p className={styles.msgText}>답변 생성 중...</p>
              </div>
            )}
            <div ref={bottomRef} />
          </div>
          <form onSubmit={handleSend} className={styles.inputBar}>
            <input
              className={styles.input}
              type="text"
              placeholder="질문을 입력하세요"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              disabled={loading}
            />
            <button className={styles.sendBtn} type="submit" disabled={loading || !input.trim()}>
              전송
            </button>
          </form>
        </div>
      )}

      <button className={styles.fab} onClick={() => setOpen((prev) => !prev)}>
        {open ? '✕' : '💬'}
      </button>
    </>
  );
}
