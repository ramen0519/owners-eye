import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { sendChat } from '../../api/insight';
import styles from './ChatPage.module.css';

export default function ChatPage() {
  const navigate = useNavigate();
  const storeId = localStorage.getItem('storeId');
  const [messages, setMessages] = useState([
    { role: 'ai', text: '안녕하세요! 매출에 관해 궁금한 것을 질문해주세요.' }
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const bottomRef = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

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
      setMessages((prev) => [...prev, { role: 'ai', text: '답변을 가져오지 못했습니다. 다시 시도해주세요.' }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <button className={styles.backBtn} onClick={() => navigate('/insight')}>← AI 인사이트</button>
        <h1 className={styles.title}>AI 채팅</h1>
        <button className={styles.nextBtn} onClick={() => navigate('/menu-sale')}>메뉴 판매 →</button>
      </header>

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
  );
}
