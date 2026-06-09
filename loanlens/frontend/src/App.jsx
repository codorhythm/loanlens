import { useState, useEffect, useRef } from "react";
import axios from "axios";
import "./App.css";

const initialForm = {
  fullName: "",
  age: "",
  monthlyIncome: "",
  monthlyDebt: "",
  creditScore: "",
  requestedLoanAmount: "",
  loanTenureMonths: "",
  employmentType: "SALARIED",
};

function ParticleCanvas() {
  const canvasRef = useRef(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    const ctx = canvas.getContext("2d");
    let animId;

    const resize = () => {
      canvas.width = window.innerWidth;
      canvas.height = window.innerHeight;
    };
    resize();
    window.addEventListener("resize", resize);

    const particles = Array.from({ length: 60 }, () => ({
      x: Math.random() * window.innerWidth,
      y: Math.random() * window.innerHeight,
      vx: (Math.random() - 0.5) * 0.3,
      vy: (Math.random() - 0.5) * 0.3,
      r: Math.random() * 1.5 + 0.5,
    }));

    const draw = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      for (let i = 0; i < particles.length; i++) {
        const p = particles[i];
        p.x += p.vx;
        p.y += p.vy;
        if (p.x < 0 || p.x > canvas.width) p.vx *= -1;
        if (p.y < 0 || p.y > canvas.height) p.vy *= -1;

        ctx.beginPath();
        ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
        ctx.fillStyle = "rgba(160, 160, 255, 0.5)";
        ctx.fill();

        for (let j = i + 1; j < particles.length; j++) {
          const q = particles[j];
          const dx = p.x - q.x;
          const dy = p.y - q.y;
          const dist = Math.sqrt(dx * dx + dy * dy);
          if (dist < 120) {
            ctx.beginPath();
            ctx.moveTo(p.x, p.y);
            ctx.lineTo(q.x, q.y);
            ctx.strokeStyle = `rgba(160, 160, 255, ${0.12 * (1 - dist / 120)})`;
            ctx.lineWidth = 0.5;
            ctx.stroke();
          }
        }
      }
      animId = requestAnimationFrame(draw);
    };
    draw();

    return () => {
      cancelAnimationFrame(animId);
      window.removeEventListener("resize", resize);
    };
  }, []);

  return <canvas ref={canvasRef} className="particle-canvas" />;
}

function TypewriterText({ text }) {
  const [displayed, setDisplayed] = useState("");

  useEffect(() => {
    setDisplayed("");
    let i = 0;
    const interval = setInterval(() => {
      if (i < text.length) {
        setDisplayed(text.slice(0, i + 1));
        i++;
      } else {
        clearInterval(interval);
      }
    }, 18);
    return () => clearInterval(interval);
  }, [text]);

  return <span>{displayed}<span className="cursor">|</span></span>;
}

export default function App() {
  const [form, setForm] = useState(initialForm);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async () => {
    setLoading(true);
    setError(null);
    setResult(null);
    try {
      const payload = {
        ...form,
        age: parseInt(form.age),
        monthlyIncome: parseFloat(form.monthlyIncome),
        monthlyDebt: parseFloat(form.monthlyDebt),
        creditScore: parseInt(form.creditScore),
        requestedLoanAmount: parseFloat(form.requestedLoanAmount),
        loanTenureMonths: parseInt(form.loanTenureMonths),
      };
      const res = await axios.post("http://localhost:8080/api/v1/loans/evaluate", payload);
      setResult(res.data);
    } catch (err) {
      setError("something went wrong. check your inputs and try again.");
    } finally {
      setLoading(false);
    }
  };

  const decisionColor = {
    APPROVED: "#22c55e",
    REJECTED: "#ef4444",
    REVIEW: "#f59e0b",
  };

  return (
      <>
        <ParticleCanvas />
        <div className="container">
          <div className="header">
            <h1>LoanLens <span className="ai-badge">AI</span></h1>
            <p className="subtitle">AI-powered loan eligibility evaluator</p>
          </div>

          <div className="form">
            <div className="form-row">
              <input name="fullName" placeholder="full name" value={form.fullName} onChange={handleChange} />
              <input name="age" placeholder="age" type="number" value={form.age} onChange={handleChange} />
            </div>
            <div className="form-row">
              <input name="monthlyIncome" placeholder="monthly income (Rs)" type="number" value={form.monthlyIncome} onChange={handleChange} />
              <input name="monthlyDebt" placeholder="monthly debt (Rs)" type="number" value={form.monthlyDebt} onChange={handleChange} />
            </div>
            <div className="form-row">
              <input name="creditScore" placeholder="credit score (300-900)" type="number" value={form.creditScore} onChange={handleChange} />
              <input name="requestedLoanAmount" placeholder="loan amount (Rs)" type="number" value={form.requestedLoanAmount} onChange={handleChange} />
            </div>
            <div className="form-row">
              <input name="loanTenureMonths" placeholder="tenure (months)" type="number" value={form.loanTenureMonths} onChange={handleChange} />
              <select name="employmentType" value={form.employmentType} onChange={handleChange}>
                <option value="SALARIED">salaried</option>
                <option value="SELF_EMPLOYED">self employed</option>
                <option value="UNEMPLOYED">unemployed</option>
              </select>
            </div>
            <button onClick={handleSubmit} disabled={loading}>
              {loading ? (
                  <span className="loading-text">
                <span className="dot-pulse" />
                AI is analysing your profile...
              </span>
              ) : "evaluate loan"}
            </button>
          </div>

          {error && <p className="error">{error}</p>}

          {result && (
              <div className="result">
                <div className="decision" style={{ color: decisionColor[result.decision] }}>
                  {result.decision}
                </div>
                <div className="meta">
                  <span>score: {result.score}/100</span>
                  <span>risk: {result.riskCategory}</span>
                  <span>emi: Rs {result.monthlyEmi}/month</span>
                </div>

                <div className="score-bar-wrap">
                  <div className="score-bar-label">eligibility score</div>
                  <div className="score-bar-bg">
                    <div className="score-bar-fill" style={{ width: `${result.score}%` }} />
                  </div>
                </div>

                <div className="explanation">
                  <div className="explanation-header">
                    <span className="claude-badge">⬡ generated by claude</span>
                  </div>
                  <p><TypewriterText text={result.aiExplanation} /></p>
                </div>

                <div className="checks">
                  {result.passedChecks.length > 0 && (
                      <div className="passed">
                        <h4>passed</h4>
                        {result.passedChecks.map((c, i) => <p key={i}>✓ {c}</p>)}
                      </div>
                  )}
                  {result.failedChecks.length > 0 && (
                      <div className="failed">
                        <h4>failed</h4>
                        {result.failedChecks.map((c, i) => <p key={i}>✗ {c}</p>)}
                      </div>
                  )}
                </div>

                <p className="app-id">application id: {result.applicationId}</p>
              </div>
          )}
        </div>
      </>
  );
}