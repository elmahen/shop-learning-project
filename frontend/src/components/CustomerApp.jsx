import { useState, useEffect } from 'react';
import { createCustomer, getAllCustomers } from '../services/customerService';

function CustomerApp() {
    const [customers, setCustomers] = useState([]);
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName]   = useState('');
    const [email, setEmail]         = useState('');
    const [error, setError]         = useState('');
    const [loading, setLoading]     = useState(false);

    
    useEffect(() => {
        loadCustomers();
    }, []);

    const loadCustomers = async () => {
        try {
            const data = await getAllCustomers();
            setCustomers(data);
        } catch (err) {
            setError('Kunden konnten nicht geladen werden.');
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await createCustomer(firstName, lastName, email);
            setFirstName('');
            setLastName('');
            setEmail('');
            await loadCustomers(); // Liste neu laden
        } catch (err) {
            if (err.response?.status === 409) {
                setError('Ein Kunde mit dieser E-Mail existiert bereits.');
            } else {
                setError('Ein Fehler ist aufgetreten.');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ maxWidth: '600px', margin: '40px auto', fontFamily: 'sans-serif' }}>
            <h1>Kunden</h1>

            {/* Formular */}
            <form onSubmit={handleSubmit}>
                <div>
                    <input
                        placeholder="Vorname"
                        value={firstName}
                        onChange={e => setFirstName(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <input
                        placeholder="Nachname"
                        value={lastName}
                        onChange={e => setLastName(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <input
                        type="email"
                        placeholder="E-Mail"
                        value={email}
                        onChange={e => setEmail(e.target.value)}
                        required
                    />
                </div>
                {error && <p style={{ color: 'red' }}>{error}</p>}
                <button type="submit" disabled={loading}>
                    {loading ? 'Wird gespeichert...' : 'Kunde erfassen'}
                </button>
            </form>

            {/* Kundenliste */}
            <h2>Alle Kunden ({customers.length})</h2>
            {customers.length === 0 ? (
                <p>Noch keine Kunden erfasst.</p>
            ) : (
                <ul>
                    {customers.map(customer => (
                        <li key={customer.id}>
                            {customer.firstName} {customer.lastName} – {customer.email}
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}

export default CustomerApp;