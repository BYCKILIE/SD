import {useEffect, useState} from 'react';
import {useNavigate} from "react-router-dom";
import {FaUser, FaLock} from "react-icons/fa";
import "../styles/login.css";
import useSignup from "../hooks/useSingup.js";
import useAuth from "../hooks/useAuth.js";

const SignupPage = () => {
    const { clientAuth, adminAuth, client, admin } = useAuth();
    const {signup, loading} = useSignup();
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const [credentials, setCredentials] = useState({
        email: '',
        password: '',
        firstName: '',
        lastName: '',
    });

    useEffect(() => {
        const handleAuth = async () => {
            await clientAuth();
            await adminAuth()
            if (admin) {
                navigate("/admin");
                return
            }
            if (client) {
                navigate("/");
            }
        }
        handleAuth();
    }, []);

    const handleChange = (e) => {
        const {name, value} = e.target;
        setCredentials((prev) => ({...prev, [name]: value}));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        try {
            await signup(credentials);
            navigate("/login");
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <div className="login-background">
            <div className="wrapper">
                <form onSubmit={handleSubmit}>
                    <h1>Signup</h1>

                    <div className={"input-box"}>
                        <input
                            type={"text"}
                            name="email"
                            placeholder={"Email or Username"}
                            value={credentials.email}
                            onChange={handleChange}
                            required
                        />
                        <FaUser className={"icon"}/>
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
                        <FaLock className={"icon"}/>
                    </div>

                    <div className={"input-box"}>
                        <input
                            type={"text"}
                            name="firstName"
                            placeholder={"First Name"}
                            value={credentials.firstName}
                            onChange={handleChange}
                            required
                        />
                        <FaUser className={"icon"}/>
                    </div>

                    <div className={"input-box"}>
                        <input
                            type={"text"}
                            name="lastName"
                            placeholder={"Last Name"}
                            value={credentials.lastName}
                            onChange={handleChange}
                            required
                        />
                        <FaUser className={"icon"}/>
                    </div>

                    {error && <p style={{color: 'red'}}>{error}</p>}
                    <button type="submit" disabled={loading}>
                        {loading ? 'Registering...' : 'Register'}
                    </button>

                </form>
            </div>
        </div>
    );
};

export default SignupPage;
