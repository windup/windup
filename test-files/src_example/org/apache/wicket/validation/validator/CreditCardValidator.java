package org.apache.wicket.validation.validator;

import org.apache.wicket.validation.*;

public class CreditCardValidator extends AbstractValidator<String>{
    private static final long serialVersionUID=1L;
    private CreditCard cardId;
    private boolean failOnUnknown;
    public CreditCardValidator(){
        super();
        this.cardId=CreditCard.INVALID;
        this.failOnUnknown=true;
    }
    public CreditCardValidator(final boolean failOnUnkown){
        super();
        this.cardId=CreditCard.INVALID;
        this.failOnUnknown=true;
        this.failOnUnknown=failOnUnkown;
    }
    public final CreditCard getCardId(){
        return this.cardId;
    }
    protected void setCardId(final CreditCard cardId){
        this.cardId=cardId;
    }
    protected void onValidate(final IValidatable<String> validatable){
        final String value=validatable.getValue();
        try{
            if(!this.isLengthAndPrefixCorrect(value)){
                this.error(validatable);
            }
        }
        catch(NumberFormatException ex){
            this.error(validatable);
        }
    }
    protected boolean isLengthAndPrefixCorrect(String creditCardNumber){
        if(creditCardNumber==null){
            return false;
        }
        creditCardNumber=creditCardNumber.replaceAll("[ -]","");
        return creditCardNumber.length()>=12&&creditCardNumber.length()<=19&&this.isChecksumCorrect(creditCardNumber)&&(!this.failOnUnknown||this.determineCardId(creditCardNumber)!=CreditCard.INVALID);
    }
    public final CreditCard determineCardId(String creditCardNumber){
        if(creditCardNumber==null){
            return CreditCard.INVALID;
        }
        creditCardNumber=creditCardNumber.replaceAll("[ -]","");
        if(creditCardNumber.length()>=12&&creditCardNumber.length()<=19&&this.isChecksumCorrect(creditCardNumber)){
            this.cardId=CreditCard.INVALID;
            if(this.cardId==CreditCard.INVALID){
                this.cardId=this.isAmericanExpress(creditCardNumber);
            }
            if(this.cardId==CreditCard.INVALID){
                this.cardId=this.isChinaUnionPay(creditCardNumber);
            }
            if(this.cardId==CreditCard.INVALID){
                this.cardId=this.isDinersClubCarteBlanche(creditCardNumber);
            }
            if(this.cardId==CreditCard.INVALID){
                this.cardId=this.isDinersClubInternational(creditCardNumber);
            }
            if(this.cardId==CreditCard.INVALID){
                this.cardId=this.isDinersClubUsAndCanada(creditCardNumber);
            }
            if(this.cardId==CreditCard.INVALID){
                this.cardId=this.isDiscoverCard(creditCardNumber);
            }
            if(this.cardId==CreditCard.INVALID){
                this.cardId=this.isJCB(creditCardNumber);
            }
            if(this.cardId==CreditCard.INVALID){
                this.cardId=this.isLaser(creditCardNumber);
            }
            if(this.cardId==CreditCard.INVALID){
                this.cardId=this.isMaestro(creditCardNumber);
            }
            if(this.cardId==CreditCard.INVALID){
                this.cardId=this.isMastercard(creditCardNumber);
            }
            if(this.cardId==CreditCard.INVALID){
                this.cardId=this.isSolo(creditCardNumber);
            }
            if(this.cardId==CreditCard.INVALID){
                this.cardId=this.isSwitch(creditCardNumber);
            }
            if(this.cardId==CreditCard.INVALID){
                this.cardId=this.isVisa(creditCardNumber);
            }
            if(this.cardId==CreditCard.INVALID){
                this.cardId=this.isVisaElectron(creditCardNumber);
            }
        }
        else{
            this.cardId=this.isUnknown(creditCardNumber);
        }
        return this.cardId;
    }
    protected CreditCard isUnknown(final String creditCardNumber){
        return CreditCard.INVALID;
    }
    private CreditCard isAmericanExpress(final String creditCardNumber){
        if(creditCardNumber.length()==15&&(creditCardNumber.startsWith("34")||creditCardNumber.startsWith("37"))){
            return CreditCard.AMERICAN_EXPRESS;
        }
        return CreditCard.INVALID;
    }
    private CreditCard isChinaUnionPay(final String creditCardNumber){
        if(creditCardNumber.length()>=16&&creditCardNumber.length()<=19&&creditCardNumber.startsWith("622")){
            final int firstDigits=Integer.parseInt(creditCardNumber.substring(0,5));
            if(firstDigits>=622126&&firstDigits<=622925){
                return CreditCard.CHINA_UNIONPAY;
            }
        }
        return CreditCard.INVALID;
    }
    private CreditCard isDinersClubCarteBlanche(final String creditCardNumber){
        if(creditCardNumber.length()==14&&creditCardNumber.startsWith("30")){
            final int firstDigits=Integer.parseInt(creditCardNumber.substring(0,3));
            if(firstDigits>=300&&firstDigits<=305){
                return CreditCard.DINERS_CLUB_CARTE_BLANCHE;
            }
        }
        return CreditCard.INVALID;
    }
    private CreditCard isDinersClubInternational(final String creditCardNumber){
        if(creditCardNumber.length()==14&&creditCardNumber.startsWith("36")){
            return CreditCard.DINERS_CLUB_INTERNATIONAL;
        }
        return CreditCard.INVALID;
    }
    private CreditCard isDinersClubUsAndCanada(final String creditCardNumber){
        if(creditCardNumber.length()==16&&(creditCardNumber.startsWith("54")||creditCardNumber.startsWith("55"))){
            return CreditCard.DINERS_CLUB_US_AND_CANADA;
        }
        return CreditCard.INVALID;
    }
    private CreditCard isDiscoverCard(final String creditCardNumber){
        if(creditCardNumber.length()==16&&creditCardNumber.startsWith("6")){
            final int firstThreeDigits=Integer.parseInt(creditCardNumber.substring(0,3));
            final int firstSixDigits=Integer.parseInt(creditCardNumber.substring(0,6));
            if(creditCardNumber.startsWith("6011")||creditCardNumber.startsWith("65")||(firstThreeDigits>=644&&firstThreeDigits<=649)||(firstSixDigits>=622126&&firstSixDigits<=622925)){
                return CreditCard.DISCOVER_CARD;
            }
        }
        return CreditCard.INVALID;
    }
    private CreditCard isJCB(final String creditCardNumber){
        if(creditCardNumber.length()==16){
            final int firstFourDigits=Integer.parseInt(creditCardNumber.substring(0,4));
            if(firstFourDigits>=3528&&firstFourDigits<=3589){
                return CreditCard.JCB;
            }
        }
        return CreditCard.INVALID;
    }
    private CreditCard isLaser(final String creditCardNumber){
        if(creditCardNumber.length()>=16&&creditCardNumber.length()<=19&&(creditCardNumber.startsWith("6304")||creditCardNumber.startsWith("6706")||creditCardNumber.startsWith("6771")||creditCardNumber.startsWith("6709"))){
            return CreditCard.LASER;
        }
        return CreditCard.INVALID;
    }
    private CreditCard isMaestro(final String creditCardNumber){
        if(creditCardNumber.length()>=12&&creditCardNumber.length()<=19&&(creditCardNumber.startsWith("5018")||creditCardNumber.startsWith("5020")||creditCardNumber.startsWith("5038")||creditCardNumber.startsWith("6304")||creditCardNumber.startsWith("6759")||creditCardNumber.startsWith("6761")||creditCardNumber.startsWith("6763"))){
            return CreditCard.MAESTRO;
        }
        return CreditCard.INVALID;
    }
    private CreditCard isSolo(final String creditCardNumber){
        if((creditCardNumber.length()==16||creditCardNumber.length()==18||creditCardNumber.length()==19)&&(creditCardNumber.startsWith("6334")||creditCardNumber.startsWith("6767"))){
            return CreditCard.SOLO;
        }
        return CreditCard.INVALID;
    }
    private CreditCard isSwitch(final String creditCardNumber){
        if((creditCardNumber.length()==16||creditCardNumber.length()==18||creditCardNumber.length()==19)&&(creditCardNumber.startsWith("4903")||creditCardNumber.startsWith("4905")||creditCardNumber.startsWith("4911")||creditCardNumber.startsWith("4936")||creditCardNumber.startsWith("564182")||creditCardNumber.startsWith("633110")||creditCardNumber.startsWith("6333")||creditCardNumber.startsWith("6759"))){
            return CreditCard.SWITCH;
        }
        return CreditCard.INVALID;
    }
    private CreditCard isVisa(final String creditCardNumber){
        if((creditCardNumber.length()==13||creditCardNumber.length()==16)&&creditCardNumber.startsWith("4")){
            return CreditCard.VISA;
        }
        return CreditCard.INVALID;
    }
    private CreditCard isVisaElectron(final String creditCardNumber){
        if(creditCardNumber.length()==16&&(creditCardNumber.startsWith("417500")||creditCardNumber.startsWith("4917")||creditCardNumber.startsWith("4913")||creditCardNumber.startsWith("4508")||creditCardNumber.startsWith("4844"))){
            return CreditCard.VISA_ELECTRON;
        }
        return CreditCard.INVALID;
    }
    private CreditCard isMastercard(final String creditCardNumber){
        if(creditCardNumber.length()==16){
            final int firstTwoDigits=Integer.parseInt(creditCardNumber.substring(0,2));
            if(firstTwoDigits>=51&&firstTwoDigits<=55){
                return CreditCard.MASTERCARD;
            }
        }
        return CreditCard.INVALID;
    }
    protected final boolean isChecksumCorrect(final String creditCardNumber){
        final int nulOffset=48;
        int sum=0;
        for(int i=1;i<=creditCardNumber.length();++i){
            int currentDigit=creditCardNumber.charAt(creditCardNumber.length()-i)-nulOffset;
            if(i%2==0){
                currentDigit*=2;
                currentDigit=((currentDigit>9)?(currentDigit-9):currentDigit);
                sum+=currentDigit;
            }
            else{
                sum+=currentDigit;
            }
        }
        return sum%10==0;
    }
    public enum CreditCard{
        INVALID((String)null),AMERICAN_EXPRESS("American Express"),CHINA_UNIONPAY("China UnionPay"),DINERS_CLUB_CARTE_BLANCHE("Diners Club Carte Blanche"),DINERS_CLUB_INTERNATIONAL("Diners Club International"),DINERS_CLUB_US_AND_CANADA("Diners Club US & Canada"),DISCOVER_CARD("Discover Card"),JCB("JCB"),LASER("Laser"),MAESTRO("Maestro"),MASTERCARD("MasterCard"),SOLO("Solo"),SWITCH("Switch"),VISA("Visa"),VISA_ELECTRON("Visa Electron");
        private final String name;
        private CreditCard(final String name){
            this.name=name;
        }
    }
}
