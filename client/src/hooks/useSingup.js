import { useContext } from 'react';
import SignupContext from "../context/SignupContext.jsx";

const useSignup = () => {
    return useContext(SignupContext);
};

export default useSignup;