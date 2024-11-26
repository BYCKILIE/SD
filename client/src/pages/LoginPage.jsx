import {useEffect, useState} from 'react';
import useAuth from '../hooks/useAuth.js';
import { useNavigate } from "react-router-dom";
import { FaUser, FaLock } from "react-icons/fa";
import "../styles/login.css";

const LoginPage = () => {
    const { clientAuth, adminAuth, client, admin, login, loading } = useAuth();
    const [credentials, setCredentials] = useState({ email: '', password: '' });
    const [error, setError] = useState('');
    const navigate = useNavigate();
    const [rememberMe, setRememberMe] = useState(false);

    useEffect(() => {
        const handleAuth = async () => {
            await clientAuth();
            await adminAuth();
            if (admin) {
                navigate("/admin");
                return;
            }
            if (client) {
                navigate("/");
            }
        };
        handleAuth();
    }, [admin, client, navigate]);

    const handleCheckbox = (event) => {
        setRememberMe(event.target.checked);
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setCredentials((prev) => ({ ...prev, [name]: value }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        const handle = async () => {
            setError('');
            try {
                await login(credentials, rememberMe);
                if (client) {
                    navigate("/");
                    return;
                }
                if (admin) {
                    navigate("/admin");
                }
            } catch (err) {
                setError(err.message);
            }
        }

        handle();
    };

    return (
        <div className="login-background">
            <div className="wrapper">
                <form onSubmit={handleSubmit}>
                    <h1>Login</h1>

                    <div className={"input-box"}>
                        <input
                            type={"text"}
                            name="email"
                            placeholder={"Email or Username"}
                            value={credentials.email}
                            onChange={handleChange}
                            required
                        />
                        <FaUser className={"icon"} />
                    </div>

                    <div className={"input-box"}>
                        <input
                            type={"password"}
                            name="password"
                            placeholder={"Password"}
                            value={credentials.password}
                            onChange={handleChange}
                            required
                        />
                        <FaLock className={"icon"} />
                    </div>

                    <div className="remember-forgot">
                        <label>
                            <input
                                type="checkbox"
                                checked={rememberMe}
                                onChange={handleCheckbox}
                            /> Remember me
                        </label>
                        <a href={"#"}>Forgot password?</a>
                    </div>

                    {error && <p style={{ color: 'red' }}>Incorrect Username or Password</p>}
                    <button type="submit" disabled={loading}>
                        {loading ? 'Logging in...' : 'Login'}
                    </button>

                    <div className="register-link">
                        <p>
                            Don&#39;t have an account?<a href={'/signup'}> Register</a>
                        </p>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default LoginPage;
