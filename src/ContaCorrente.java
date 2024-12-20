public class ContaCorrente {
    int numero = 0;
    double saldo = 0;
    double limite = 0;

    ContaCorrente(int numero, double saldo, double limite) {
        this.numero = numero;
        this.saldo = saldo;
        this.limite = limite;
    }

    public int getNumero() {
        return this.numero;
    }

    public double getSaldo() {
        return this.saldo;
    }
    
    public double getLimite() {
        return this.limite;
    }
    
    public void setLimite(double limite) {
        if (limite < 0) throw new IllegalArgumentException("O valor do limite deve ser positivo.");
        
        this.limite = limite;
    }

    public void depositar(double quantia) {
        if (quantia < 0) throw new IllegalArgumentException("O valor do depósito deve ser positivo.");

        this.saldo += quantia;
    }

    public void sacar(double quantia, boolean consulta) {
        if (quantia < 0) throw new IllegalArgumentException("O valor do saque deve ser positivo.");
        if (quantia > this.saldo + this.limite) throw new IllegalArgumentException("Saldo insuficiente para saque.");

        // se consulta for verdadeiro, apenas verificar se o saque seria possível
        if (!consulta) {
            this.saldo -= quantia;
        }
    }
}