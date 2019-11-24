/**
 * This file is generated by xresloader 2.7.0, please don't edit it.
 * You can find more information about this xresloader on https://xresloader.atframe.work/ .
 * If there is any problem, please find or report issues on https://github.com/xresloader/xresloader/issues .
 */
#pragma once

#include "CoreMinimal.h"
#include "UObject/ConstructorHelpers.h"
#include "Engine/DataTable.h"
#include "KindConst.generated.h"


USTRUCT(BlueprintType)
struct FKindConst : public FTableRowBase
{
    GENERATED_USTRUCT_BODY()

    // Start of fields
    /** Field Type: STRING, Name: Name. This field is generated for UE Editor compatible. **/
    UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "XResConfig")
    FName Name;

    /** Field Type: STRING, Name: Value. This field is generated for UE Editor compatible. **/
    UPROPERTY(EditAnywhere, BlueprintReadOnly, Category = "XResConfig")
    FString Value;

};


UCLASS(Blueprintable, BlueprintType)
class UKindConstHelper : public UObject
{
    GENERATED_BODY()

public:
    UKindConstHelper();

    void OnReload();

    static FName GetRowName(FName Name);

    UFUNCTION(BlueprintCallable, Category = "XResConfig")
    FName GetDataRowName(FName Name) const;

    UFUNCTION(BlueprintCallable, Category = "XResConfig")
    FName GetTableRowName(const FKindConst& TableRow) const;

    UFUNCTION(BlueprintCallable, Category = "XResConfig")
    const FKindConst& GetDataRowByName(const FName& Name, bool& IsValid) const;

    UFUNCTION(BlueprintCallable, Category = "XResConfig")
    const FKindConst& GetDataRowByKey(FName Name, bool& IsValid) const;

    bool ForeachRow(TFunctionRef<void (const FName& Key, const FKindConst& Value)> Predicate) const;

    UFUNCTION(BlueprintCallable, Category = "XResConfig")
    UDataTable* GetRawDataTable(bool& IsValid) const;

    static void ClearRow(FKindConst& TableRow);

    UFUNCTION(BlueprintCallable, Category = "XResConfig")
    void ClearDataRow(FKindConst& TableRow) const;

private:
    TSharedPtr<ConstructorHelpers::FObjectFinder<UDataTable> > Loader;
    UDataTable* DataTable;
    FKindConst Empty;
};

