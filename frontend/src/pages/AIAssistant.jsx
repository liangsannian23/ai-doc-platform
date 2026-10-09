import { useEffect, useState } from 'react'
import { aiApi } from '../api/ai'
import { documentApi } from '../api/documents'

const TABS = [
  { key: 'summary', label: '文档摘要' },
  { key: 'proofread', label: '文本纠错' },
  { key: 'polish', label: '内容润色' },
  { key: 'qa', label: '文档问答' },
]

export default function AIAssistant() {
  const [tab, setTab] = useState('summary')

  return (
    <div className="page">
      <div className="page-head">
        <h2>AI 助手</h2>
      </div>
      <div className="tabs">
        {TABS.map((t) => (
          <button
            key={t.key}
            className={'tab' + (tab === t.key ? ' active' : '')}
            onClick={() => setTab(t.key)}
          >
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'summary' && <SummaryPane />}
      {tab === 'proofread' && <ProofreadPane />}
      {tab === 'polish' && <PolishPane />}
      {tab === 'qa' && <QaPane />}
    </div>
  )
}

function useText() {
  const [content, setContent] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  return { content, setContent, error, setError, loading, setLoading }
}

function SummaryPane() {
  const { content, setContent, error, setError, loading, setLoading } = useText()
  const [result, setResult] = useState('')

  async function run() {
    setLoading(true)
    setError('')
    try {
      setResult(await aiApi.summary(content))
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="pane">
      <textarea
        className="input input-content"
        rows={10}
        placeholder="粘贴需要生成摘要的文本（≤2000 字）"
        value={content}
        onChange={(e) => setContent(e.target.value)}
      />
      <RunButton onClick={run} loading={loading} disabled={!content.trim()} />
      {error && <div className="alert alert-error">{error}</div>}
      {result && <div className="ai-result">{result}</div>}
    </div>
  )
}

function ProofreadPane() {
  const { content, setContent, error, setError, loading, setLoading } = useText()
  const [result, setResult] = useState(null)

  async function run() {
    setLoading(true)
    setError('')
    try {
      setResult(await aiApi.proofread(content))
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="pane">
      <textarea
        className="input input-content"
        rows={10}
        placeholder="粘贴需要纠错的文本"
        value={content}
        onChange={(e) => setContent(e.target.value)}
      />
      <RunButton onClick={run} loading={loading} disabled={!content.trim()} />
      {error && <div className="alert alert-error">{error}</div>}
      {result && (
        <div className="proofread-result">
          {result.errors?.length > 0 ? (
            <ul className="error-list">
              {result.errors.map((e, i) => (
                <li key={i}>
                  <span className="wrong">{e.wrong}</span> → <span className="right">{e.right}</span>
                  {e.reason && <em>（{e.reason}）</em>}
                </li>
              ))}
            </ul>
          ) : (
            <div className="alert alert-success">未发现明显错误</div>
          )}
          {result.correctedText && (
            <div className="ai-result">
              <strong>纠正后：</strong>
              {result.correctedText}
            </div>
          )}
        </div>
      )}
    </div>
  )
}

function PolishPane() {
  const { content, setContent, error, setError, loading, setLoading } = useText()
  const [style, setStyle] = useState('专业')
  const [result, setResult] = useState('')

  async function run() {
    setLoading(true)
    setError('')
    try {
      setResult(await aiApi.polish(content, style))
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="pane">
      <div className="field-inline">
        <span>风格</span>
        <select className="input" value={style} onChange={(e) => setStyle(e.target.value)}>
          <option value="专业">专业</option>
          <option value="幽默">幽默</option>
          <option value="简洁">简洁</option>
        </select>
      </div>
      <textarea
        className="input input-content"
        rows={10}
        placeholder="粘贴需要润色的文本"
        value={content}
        onChange={(e) => setContent(e.target.value)}
      />
      <RunButton onClick={run} loading={loading} disabled={!content.trim()} />
      {error && <div className="alert alert-error">{error}</div>}
      {result && <div className="ai-result">{result}</div>}
    </div>
  )
}

function QaPane() {
  const { error, setError, loading, setLoading } = useText()
  const [docs, setDocs] = useState([])
  const [docId, setDocId] = useState('')
  const [question, setQuestion] = useState('')
  const [answer, setAnswer] = useState('')

  useEffect(() => {
    documentApi
      .page({ current: 1, size: 100 })
      .then((p) => setDocs(p.records || []))
      .catch(() => {})
  }, [])

  async function run(e) {
    e.preventDefault()
    if (!docId || !question.trim()) return
    setLoading(true)
    setError('')
    try {
      setAnswer(await aiApi.qa(Number(docId), question))
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <form className="pane" onSubmit={run}>
      <label className="field">
        <span>选择文档</span>
        <select className="input" value={docId} onChange={(e) => setDocId(e.target.value)}>
          <option value="">请选择…</option>
          {docs.map((d) => (
            <option key={d.id} value={d.id}>
              {d.title}
            </option>
          ))}
        </select>
      </label>
      <input
        className="input"
        placeholder="输入你的问题"
        value={question}
        onChange={(e) => setQuestion(e.target.value)}
      />
      <RunButton loading={loading} disabled={!docId || !question.trim()} type="submit" label="提问" />
      {error && <div className="alert alert-error">{error}</div>}
      {answer && <div className="ai-result ai-answer">{answer}</div>}
    </form>
  )
}

function RunButton({ onClick, loading, disabled, type = 'button', label = '执行' }) {
  return (
    <button className="btn btn-primary" type={type} onClick={onClick} disabled={loading || disabled}>
      {loading ? '处理中…' : label}
    </button>
  )
}
